package org.keycloak.broker.bankid;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.CacheControl;

import org.infinispan.Cache;
import org.jboss.logging.Logger;
import org.keycloak.broker.bankid.client.BankidClientException;
import org.keycloak.broker.bankid.client.SimpleBankidClient;
import org.keycloak.broker.bankid.model.AuthResponse;
import org.keycloak.broker.bankid.model.BankidHintCodes;
import org.keycloak.broker.bankid.model.BankidUser;
import org.keycloak.broker.bankid.model.CollectResponse;
import org.keycloak.broker.bankid.model.CompletionData;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.sessions.AuthenticationSessionModel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.wildfly.security.http.HttpServerRequest;

public class BankidEndpoint {

	private final BankidIdentityProviderConfig config;
	private final AuthenticationCallback callback;
	private final BankidIdentityProvider provider;
	private final SimpleBankidClient bankidClient;
	private static final Logger logger = Logger.getLogger(BankidEndpoint.class);

	// The maximum number of minutes to store bankid session info in the token cache
	// Setting this to 5 since BankID will timeout after 3 minutes
	private static final long MAX_CACHE_LIFESPAN = 5;

	private final Cache<Object, Object> actionTokenCache;

	private static final String qrCodePrefix = "bankid.";

	@Context
	protected KeycloakSession session;

	public BankidEndpoint(BankidIdentityProvider provider, BankidIdentityProviderConfig config,
			AuthenticationCallback callback) {
		this.config = config;
		this.callback = callback;
		this.provider = provider;
		this.bankidClient = new SimpleBankidClient(provider.buildBankidHttpClient(), config.getApiUrl());
		InfinispanConnectionProvider infinispanConnectionProvider = provider.getSession()
				.getProvider(InfinispanConnectionProvider.class);
		this.actionTokenCache = infinispanConnectionProvider.getCache(InfinispanConnectionProvider.ACTION_TOKEN_CACHE);
	}

	@GET
	@Path("/start")
	public Response start(@QueryParam("state") String state) {

		if (state == null) {
			return callback.error("bankid.hints." + BankidHintCodes.internal.messageShortName);
		}

		if (config.isRequiredNin()) {
			LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);
			return loginFormsProvider
					.setAttribute("state", state)
					.createForm("start-bankid.ftl");
		} else {
			// Go direct to login if we do not require non.
			return doLogin(null, state);
		}
	}

    @GET
    @Path("/api/start")
    public Response apiStart(@QueryParam("state") String state) {
        try {
            AuthResponse authResponse = bankidClient.sendAuth(null, session.getContext().getConnection().getRemoteAddr());

            UUID bankidRef = UUID.randomUUID();
            this.actionTokenCache.put(bankidRef.toString(), authResponse, MAX_CACHE_LIFESPAN, TimeUnit.MINUTES);
            URI pollingUri = provider.redirectUriBuilder()
                    .path("/api/collect")
                    .queryParam("bankidref", bankidRef)
					.queryParam("state", state)
				.build();

            URI cancelUri = provider.redirectUriBuilder()
                    .path("/api/cancel")
                    .queryParam("bankidref", bankidRef)
					.queryParam("state", state)
				.build();

            return Response.ok(String.format("{ \"pollingUrl\": \"%s\", \"cancelUrl\": \"%s\", \"autostarttoken\": \"%s\" }",
                    pollingUri, cancelUri, authResponse.getAutoStartToken()),
                MediaType.APPLICATION_JSON_TYPE).build();

        } catch (BankidClientException e) {
			BankidHintCodes hintCode = e.getHintCode();
            return Response
				.status(Status.INTERNAL_SERVER_ERROR).entity(String
                    .format("{ \"status\": \"%s\", \"hintCode\": \"%s\", \"messageShortName\": \"%s\" }",
                            "failed", hintCode, hintCode.messageShortName))
				.type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

	@POST
	@Path("/login")
	public Response loginPost(@FormParam("nin") String nin, @FormParam("state") String state) {
		return doLogin(nin, state);
	}

	@GET
	@Path("/login")
	public Response loginGet(@QueryParam("state") String state) {
		return doLogin(null, state);
	}

	private Response doLogin(String nin, String state) {
		LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);

		try {
			AuthResponse authResponse;
			authResponse = bankidClient.sendAuth(nin, session.getContext().getConnection().getRemoteAddr());

			UUID bankidRef = UUID.randomUUID();
			this.actionTokenCache.put(bankidRef.toString(), authResponse, MAX_CACHE_LIFESPAN, TimeUnit.MINUTES);
			return loginFormsProvider
					.setAttribute("bankidref", bankidRef.toString())
					.setAttribute("state", state)
					.setAttribute("autoStartToken", authResponse.getAutoStartToken())
					.setAttribute("showqr", config.isShowQRCode()).setAttribute("ninRequired", config.isRequiredNin())
					.createForm("login-bankid.ftl");
		} catch (BankidClientException e) {
			return loginFormsProvider.setError("bankid.hints." + e.getHintCode().messageShortName)
					.createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/collect")
	public Response collect(@QueryParam("bankidref") String bankidRef) {
		if (!this.actionTokenCache.containsKey(bankidRef) ||
				!(this.actionTokenCache.get(bankidRef) instanceof AuthResponse)) {
			LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);
			return loginFormsProvider.setError("bankid.error.internal").createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}

        String orderref = ((AuthResponse) this.actionTokenCache.get(bankidRef)).getOrderRef();
        try {
            CollectResponse responseData = bankidClient.sendCollect(orderref);
            if ("failed".equalsIgnoreCase(responseData.getStatus())) {
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }",
                                responseData.getStatus(), responseData.getHintCode()))
                        .type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                if ("complete".equalsIgnoreCase(responseData.getStatus())) {
                    this.actionTokenCache.put(bankidRef + "-completion", responseData.getCompletionData());
                }
                return Response.ok(String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }",
                                responseData.getStatus(), responseData.getHintCode()), MediaType.APPLICATION_JSON_TYPE)
                        .build();
            }
        } catch (BankidClientException e) {
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR).entity(String
                            .format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }", "failed", e.getHintCode()))
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }
	}

    @GET
    @Path("/api/collect")
    public Response apiCollect(@QueryParam("state") String state, @QueryParam("bankidref") String bankidRef) {
        if (!this.actionTokenCache.containsKey(bankidRef) ||
                !(this.actionTokenCache.get(bankidRef) instanceof AuthResponse)) {
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR).entity(String
                            .format("{ \"status\": \"%s\", \"hintCode\": \"%s\", \"messageShortName\": null, \"completionUrl\": null }", "500", "bankid.error.internal"))
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        String orderref = ((AuthResponse) this.actionTokenCache.get(bankidRef)).getOrderRef();
        try {
            CollectResponse responseData = bankidClient.sendCollect(orderref);
            if ("failed".equalsIgnoreCase(responseData.getStatus())) {
				BankidHintCodes hintCode = BankidHintCodes.valueOf(responseData.getHintCode());
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\", \"messageShortName\": \"%s\", \"completionUrl\": null }",
                                responseData.getStatus(), hintCode, hintCode.messageShortName))
                        .type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                if ("complete".equalsIgnoreCase(responseData.getStatus())) {
                    this.actionTokenCache.put(bankidRef + "-completion", responseData.getCompletionData());
					URI completionUrl = provider.redirectUriBuilder()
						.path("api/done")
						.queryParam("bankidref", bankidRef)
						.queryParam("state", state)
					.build();

					return Response.ok(String.format("{ \"status\": \"%s\", \"hintCode\": null, \"messageShortName\": null, \"completionUrl\": \"%s\" }",
									responseData.getStatus(), completionUrl), MediaType.APPLICATION_JSON_TYPE)
							.build();
                } else {
					BankidHintCodes hintCode = BankidHintCodes.valueOf(responseData.getHintCode());
					return Response.ok(String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\", \"messageShortName\": \"%s\", \"completionUrl\": null }",
							responseData.getStatus(), hintCode, hintCode.messageShortName), MediaType.APPLICATION_JSON_TYPE)
						.build();
				}
            }
        } catch (BankidClientException e) {
			BankidHintCodes hintCode = e.getHintCode();
            return Response
				.status(Status.INTERNAL_SERVER_ERROR).entity(String
						.format("{ \"status\": \"%s\", \"hintCode\": \"%s\", \"messageShortName\": \"%s\", \"completionUrl\": null }", "failed", hintCode, hintCode.messageShortName))
				.type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

	@GET
	@Path("/done")
	public Response done(@QueryParam("state") String state, @QueryParam("bankidref") String bankidRef) {
		LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);

		if (!this.actionTokenCache.containsKey(bankidRef + "-completion") ||
				!(this.actionTokenCache.get(bankidRef + "-completion") instanceof CompletionData)) {
			logger.error("Action token cache does not have a CompletionData object.");
			return loginFormsProvider.setError("bankid.error.internal").createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}

		return completeAuth(state, bankidRef);
	}

	@GET
	@Path("/api/done")
	public Response apiDone(@QueryParam("state") String state, @QueryParam("bankidref") String bankidRef) {

		if (!this.actionTokenCache.containsKey(bankidRef + "-completion") ||
				!(this.actionTokenCache.get(bankidRef + "-completion") instanceof CompletionData)) {
			logger.error("Action token cache does not have a CompletionData object.");
			return Response
				.status(Status.INTERNAL_SERVER_ERROR).entity(String
						.format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }", "500", "bankid.error.internal"))
				.type(MediaType.APPLICATION_JSON_TYPE).build();
		}

		return completeAuth(state, bankidRef);
	}

	private Response completeAuth(String state, String bankidRef) {
		CompletionData completionData = (CompletionData) this.actionTokenCache.get(bankidRef + "-completion");
		BankidUser user = completionData.getUser();
		// Make sure to remove the authresponse attribute from the session
		try {
			AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
			session.getContext().setAuthenticationSession(authSession);
			BrokeredIdentityContext identity = new BrokeredIdentityContext(
					getConfig().getAlias().concat("." + getUsername(user)));

			identity.setIdpConfig(config);
			identity.setIdp(provider);
			identity.setUsername(getUsername(user));
			identity.setFirstName(user.getGivenName());
			identity.setLastName(user.getSurname());

			// Set user session notes
			authSession.setUserSessionNote(getConfig().getAlias().concat(".pnr"), getUsername(user));
			authSession.setUserSessionNote(getConfig().getAlias().concat(".issuedate"),
					completionData.getBankIdIssueDate());
			authSession.setUserSessionNote(getConfig().getAlias().concat(".device.ipaddress"),
					completionData.getDevice().getIpAddress());
			authSession.setUserSessionNote(getConfig().getAlias().concat(".ocspresponse"),
					completionData.getOcspResponse());
			identity.setAuthenticationSession(authSession);

			return callback.authenticated(identity);
		} catch (Exception e) {
			throw new RuntimeException("Failed to decode user information.", e);
		}
	}

	private String getUsername(BankidUser user) throws NoSuchAlgorithmException {
		if (this.config.isSaveNinHashed()) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(user.getPersonalNumber().getBytes());
			return Base64.getEncoder().encodeToString(md.digest());
		} else {
			return user.getPersonalNumber();
		}
	}

	@GET
	@Path("/cancel")
	public Response cancel(@QueryParam("bankidref") String bankidRef) {
		LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);

		if (!this.actionTokenCache.containsKey(bankidRef) ||
				!(this.actionTokenCache.get(bankidRef) instanceof AuthResponse)) {
			return loginFormsProvider.setError("bankid.error.internal").createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}

		if (!this.actionTokenCache.containsKey(bankidRef) ||
				!(this.actionTokenCache.get(bankidRef) instanceof AuthResponse)) {
			return loginFormsProvider.setError("bankid.error.internal").createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}

		AuthResponse authResponse = (AuthResponse) this.actionTokenCache.get(bankidRef);
		if (authResponse != null) {
			String orderRef = authResponse.getOrderRef();
			bankidClient.sendCancel(orderRef);
		}
		return loginFormsProvider.setError("bankid.hints." + BankidHintCodes.cancelled.messageShortName)
				.createErrorPage(Status.INTERNAL_SERVER_ERROR);
	}

	@GET
	@Path("/api/cancel")
	public Response apiCancel(@QueryParam("bankidref") String bankidRef) {

		if (!this.actionTokenCache.containsKey(bankidRef) ||
				!(this.actionTokenCache.get(bankidRef) instanceof AuthResponse)) {
			return Response
				.status(Status.INTERNAL_SERVER_ERROR).entity(String
						.format("{ \"status\": \"%s\", \"hintCode\": \"%s\", \"messageShortName\": null, }", "500", "bankid.error.internal"))
				.type(MediaType.APPLICATION_JSON_TYPE).build();
		}

		AuthResponse authResponse = (AuthResponse) this.actionTokenCache.get(bankidRef);
		if (authResponse != null) {
			String orderRef = authResponse.getOrderRef();
			bankidClient.sendCancel(orderRef);
		}

		return Response
			.ok(String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\", \"messageShortName\": \"%s\" }", "cancelled",
					BankidHintCodes.cancelled, BankidHintCodes.cancelled.messageShortName))
			.type(MediaType.APPLICATION_JSON_TYPE).build();
	}

	@GET
	@Path("/error")
	public Response error(@QueryParam("code") String hintCode) {

		BankidHintCodes hint;
		// Sanitize input from the web
		try {
			hint = BankidHintCodes.valueOf(hintCode);
		} catch (IllegalArgumentException e) {
			hint = BankidHintCodes.unkown;
		}
		LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);
		return loginFormsProvider.setError("bankid.hints." + hint.messageShortName)
				.createErrorPage(Status.INTERNAL_SERVER_ERROR);
	}

	@GET
	@Path("/qrcode")
	public Response qrcode(@QueryParam("bankidref") String bankidRef) {
		if (this.actionTokenCache.containsKey(bankidRef)) {
			LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);
			if (!this.actionTokenCache.containsKey(bankidRef) &&
					!(this.actionTokenCache.get(bankidRef) instanceof AuthResponse)) {
				return loginFormsProvider.setError("bankid.error.internal")
						.createErrorPage(Status.INTERNAL_SERVER_ERROR);
			}

			AuthResponse authResponse = (AuthResponse) this.actionTokenCache.get(bankidRef);
			long elapsedTime = (System.currentTimeMillis() / 1000) - authResponse.getAuthTimestamp();

			String qrAuthCode;
			try {
				qrAuthCode = generateQrAuthCode(authResponse.getQrStartSecret(), Long.toString(elapsedTime));
			} catch (Exception e) {
				logger.error("Failed to generate qrAuthCode");
				throw new RuntimeException(e);
			}

			String qrCode = qrCodePrefix + authResponse.getQrStartToken() + "." + elapsedTime + "." + qrAuthCode;

			try {

				int width = 246;
				int height = 246;

				QRCodeWriter writer = new QRCodeWriter();
				final BitMatrix bitMatrix = writer.encode(
						qrCode, BarcodeFormat.QR_CODE, width,
						height);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				MatrixToImageWriter.writeToStream(bitMatrix, "png", bos);
				bos.close();

				CacheControl cc = new CacheControl();
				cc.setNoStore(true);

				ResponseBuilder builder = Response.ok(bos.toByteArray(), "image/png");
				builder.cacheControl(cc);
				return builder.build();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return Response.serverError().build();
	}

	public BankidIdentityProviderConfig getConfig() {
		return config;
	}

	private String generateQrAuthCode(String qrStartSecret, String time)
			throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(qrStartSecret.getBytes("ascii"), "HmacSHA256"));

		return String.format("%064x", new BigInteger(1, mac.doFinal(new String(time).getBytes())));
	}
}
