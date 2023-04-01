package org.keycloak.broker.bankid;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.infinispan.Cache;
import org.jboss.logging.Logger;
import org.keycloak.broker.bankid.client.BankidClientException;
import org.keycloak.broker.bankid.client.SimpleBankidClient;
import org.keycloak.broker.bankid.model.AuthResponse;
import org.keycloak.broker.bankid.model.BankidHintCodes;
import org.keycloak.broker.bankid.model.BankidUser;
import org.keycloak.broker.bankid.model.CollectResponse;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.sessions.AuthenticationSessionModel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class BankidEndpoint {

	private BankidIdentityProviderConfig config;
	private AuthenticationCallback callback;
	private BankidIdentityProvider provider;
	private SimpleBankidClient bankidClient;
	private static final Logger logger = Logger.getLogger(BankidEndpoint.class);

	private Cache<Object, Object> actionTokenCache ;

	@Context
	protected KeycloakSession session;

	public BankidEndpoint(BankidIdentityProvider provider, BankidIdentityProviderConfig config,
			AuthenticationCallback callback) {
		this.config = config;
		this.callback = callback;
		this.provider = provider;
		this.bankidClient = new SimpleBankidClient(provider.buildBankidHttpClient(), config.getApiUrl());
		InfinispanConnectionProvider infinispanConnectionProvider =
                provider.getSession().getProvider(InfinispanConnectionProvider.class);
    	this.actionTokenCache =
            infinispanConnectionProvider.getCache(InfinispanConnectionProvider.ACTION_TOKEN_CACHE);
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

		// TODO: Make sure we do not have a session already running
		try {
			AuthResponse authResponse;
			authResponse = bankidClient.sendAuth(nin, session.getContext().getConnection().getRemoteAddr());
			
			UUID bankidRef = UUID.randomUUID();
			// TODO: We should put a life span on this .... 
			// this.actionTokenCache.put(bankidRef.toString(), authResponse, lifespan, lifespanunit);
			this.actionTokenCache.put(bankidRef.toString(), authResponse);
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

		if (this.actionTokenCache.containsKey(bankidRef)) {
			String orderref = ((AuthResponse) this.actionTokenCache.get(bankidRef)).getOrderRef();
			try {
				CollectResponse responseData = bankidClient.sendCollect(orderref);
				// Check responseData.getStatus()
				if ("failed".equalsIgnoreCase(responseData.getStatus())) {
					return Response.status(Status.INTERNAL_SERVER_ERROR)
							.entity(String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }",
									responseData.getStatus(), responseData.getHintCode()))
							.type(MediaType.APPLICATION_JSON_TYPE).build();
				} else {
					if ("complete".equalsIgnoreCase(responseData.getStatus())) {
						this.actionTokenCache.put(bankidRef, responseData.getCompletionData().getUser());
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
		} else {
			return Response.ok(String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }", "500", "internal"),
					MediaType.APPLICATION_JSON_TYPE).build();
		}
	}

	@GET
	@Path("/done")
	public Response done(@QueryParam("state") String state, @QueryParam("bankidref") String bankidRef) {
		LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);

		if (!this.actionTokenCache.containsKey(bankidRef) ||
			!(this.actionTokenCache.get(bankidRef) instanceof BankidUser)) {
			logger.error("Session attribute 'bankidUser' not set or not correct type.");
			return loginFormsProvider.setError("bankid.error.internal").createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}

		BankidUser user = (BankidUser) this.actionTokenCache.get(bankidRef);
		// Make sure to remove the authresponse attribute from the session
		try {
			AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
			session.getContext().setAuthenticationSession(authSession);
			BrokeredIdentityContext identity = new BrokeredIdentityContext(getUsername(user));

			identity.setIdpConfig(config);
			identity.setIdp(provider);
			identity.setUsername(getUsername(user));
			identity.setFirstName(user.getGivenName());
			identity.setLastName(user.getSurname());
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
	public Response canel(@QueryParam("bankidref") String bankidRef) {
		LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);
		logger.error(String.format("%s = %s", bankidRef, this.actionTokenCache.get(bankidRef)));

		if (!this.actionTokenCache.containsKey(bankidRef) ||
			!(this.actionTokenCache.get(bankidRef) instanceof AuthResponse)) {
			logger.error("Session attribute 'bankidUser' not set or not correct type.");
			return loginFormsProvider.setError("bankid.error.internal").createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}

		AuthResponse authResponse = (AuthResponse) this.actionTokenCache.get(bankidRef);
		if (authResponse != null) {
			String orderRef = authResponse.getOrderRef();
			bankidClient.sendCancel(orderRef);
		}
		// Make sure to remove the authresponse attribute from the session
		return callback.error("bankid.hints." + BankidHintCodes.cancelled.messageShortName);
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
		logger.error(String.format("%s = %s", bankidRef, this.actionTokenCache.get(bankidRef)));
		
		if (this.actionTokenCache.containsKey(bankidRef)) {
			LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);
			if (!this.actionTokenCache.containsKey(bankidRef) &&
				!(this.actionTokenCache.get(bankidRef) instanceof AuthResponse)) {
				logger.error("Session attribute 'bankidUser' not set or not correct type.");
				return loginFormsProvider.setError("bankid.error.internal").createErrorPage(Status.INTERNAL_SERVER_ERROR);
			}
	
			AuthResponse authResponse = (AuthResponse) this.actionTokenCache.get(bankidRef);
			String autostarttoken = authResponse.getAutoStartToken();
			try {

				int width = 246;
				int height = 246;

				QRCodeWriter writer = new QRCodeWriter();
				final BitMatrix bitMatrix = writer.encode(
						"bankid:///?autostarttoken=" + autostarttoken, BarcodeFormat.QR_CODE, width,
						height);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				MatrixToImageWriter.writeToStream(bitMatrix, "png", bos);
				bos.close();

				return Response.ok(bos.toByteArray(), "image/png").build();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return Response.serverError().build();
	}

	public BankidIdentityProviderConfig getConfig() {
		return config;
	}
}
