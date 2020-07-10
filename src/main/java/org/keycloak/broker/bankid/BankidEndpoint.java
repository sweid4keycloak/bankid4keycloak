package org.keycloak.broker.bankid;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;
import org.keycloak.broker.bankid.client.BankidClientException;
import org.keycloak.broker.bankid.client.SimpleBankidClient;
import org.keycloak.broker.bankid.model.AuthResponse;
import org.keycloak.broker.bankid.model.BankidHintCodes;
import org.keycloak.broker.bankid.model.BankidUser;
import org.keycloak.broker.bankid.model.CollectResponse;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;
import org.keycloak.forms.login.LoginFormsProvider;

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

	public BankidEndpoint(BankidIdentityProvider provider, BankidIdentityProviderConfig config,
			AuthenticationCallback callback) {
		this.config = config;
		this.callback = callback;
		this.provider = provider;
		this.bankidClient = new SimpleBankidClient(provider.buildBankidHttpClient(), config.getApiUrl());
	}

	@GET
	@Path("/start")
	public Response start(@QueryParam("state") String state, @Context HttpServletRequest request) {

		state = getOrSetStringFromSession(request.getSession(), "bankid.state", state);

		if (state == null) {
			return callback.error(state, "bankid.hints." + BankidHintCodes.internal.messageShortName);
		}
		if (config.isRequiredNin()) {
			LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);
			return loginFormsProvider.createForm("start-bankid.ftl");
		} else {
			// Go direct to login if we do not require non.
			return doLogin(null, request);
		}
	}

	@POST
	@Path("/login")
	public Response loginPost(@FormParam("nin") String nin, @Context HttpServletRequest request) {
		return doLogin(nin, request);
	}

	@GET
	@Path("/login")
	public Response loginGet(@Context HttpServletRequest request) {
		return doLogin(null, request);
	}

	private Response doLogin(String nin, @Context HttpServletRequest request) {
		LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);

		String state = getOrSetStringFromSession(request.getSession(), "bankid.state", null);
		if (state == null) {
			clearAllBankidFromSession(request.getSession());
			return loginFormsProvider.setError("bankid.hints." + BankidHintCodes.internal.messageShortName)
					.createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}
		try {
			AuthResponse authResponse;
			if (request.getSession().getAttribute("bankid.authresponse") == null) {
				authResponse = bankidClient.sendAuth(nin, request.getRemoteAddr());
				request.getSession().setAttribute("bankid.authresponse", authResponse);
			} else {
				authResponse = (AuthResponse) request.getSession().getAttribute("bankid.authresponse");
			}
			return loginFormsProvider.setAttribute("state", state)
					.setAttribute("autoStartToken", authResponse.getAutoStartToken())
					.setAttribute("showqr", config.isShowQRCode())
					.setAttribute("ninRequired", config.isRequiredNin())
					.createForm("login-bankid.ftl");
		} catch (BankidClientException e) {
			clearAllBankidFromSession(request.getSession());
			return loginFormsProvider.setError("bankid.hints." + e.getHintCode().messageShortName)
					.createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/collect")
	public Response collect(@Context HttpServletRequest request) {
		if (request.getSession().getAttribute("bankid.authresponse") != null) {
			String orderref = ((AuthResponse) request.getSession().getAttribute("bankid.authresponse")).getOrderRef();
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
						request.getSession().removeAttribute("bankid.authresponse");
						request.getSession().setAttribute("bankid.user", responseData.getCompletionData().getUser());
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
	public Response done(@Context HttpServletRequest request) {

		String state = getOrSetStringFromSession(request.getSession(), "bankid.state", null);
		if (state == null) {
			clearAllBankidFromSession(request.getSession());
			return callback.error(state, "bankid.hints." + BankidHintCodes.internal.messageShortName);
		}

		LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);

		if (request.getSession().getAttribute("bankid.user") == null
				|| !(request.getSession().getAttribute("bankid.user") instanceof BankidUser)) {
			logger.error("Session attribute 'bankidUser' not set or not correct type.");
			clearAllBankidFromSession(request.getSession());
			return loginFormsProvider.setError("bankid.error.internal").createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}
		BankidUser user = (BankidUser) request.getSession().getAttribute("bankid.user");

		// Make sure to remove the authresponse attribute from the session
		clearAllBankidFromSession(request.getSession());
		try {
			BrokeredIdentityContext identity = new BrokeredIdentityContext(getUsername(user));

			identity.setIdpConfig(config);
			identity.setIdp(provider);
			identity.setUsername(getUsername(user));
			identity.setFirstName(user.getGivenName());
			identity.setLastName(user.getSurname());
			identity.setCode(state);

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
	public Response canel(@Context HttpServletRequest request) {

		String state = getOrSetStringFromSession(request.getSession(), "bankid.state", null);
		if (state == null) {
			clearAllBankidFromSession(request.getSession());
			return callback.error(state, "bankid.hints." + BankidHintCodes.internal.messageShortName);
		}
		AuthResponse authResponse = (AuthResponse) request.getSession().getAttribute("bankid.authresponse");
		if ( authResponse != null ) {
			String orderRef = authResponse.getOrderRef();
			bankidClient.sendCancel(orderRef);
		}
		// Make sure to remove the authresponse attribute from the session
		clearAllBankidFromSession(request.getSession());
		return callback.error(state, "bankid.hints." + BankidHintCodes.cancelled.messageShortName);
	}

	@GET
	@Path("/error")
	public Response error(@QueryParam("code") String hintCode, @Context HttpServletRequest request) {
		// Make sure to remove the authresponse attribute from the session
		clearAllBankidFromSession(request.getSession());

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
	public Response qrcode(@Context HttpServletRequest request) {
		AuthResponse authResponse;
		if (request.getSession().getAttribute("bankid.authresponse") != null) {
			authResponse = (AuthResponse) request.getSession().getAttribute("bankid.authresponse");
			try {

				int width = 246;
				int height = 246;

				QRCodeWriter writer = new QRCodeWriter();
				final BitMatrix bitMatrix = writer.encode(
						"bankid:///?autostarttoken=" + authResponse.getAutoStartToken(), BarcodeFormat.QR_CODE, width,
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

	private String getOrSetStringFromSession(HttpSession session, String name, String defaultValue) {
		if (session.getAttribute(name) == null) {
			session.setAttribute(name, defaultValue);
		}
		Object value = session.getAttribute(name);
		return (value == null ? null : value.toString());
	}

	private void clearAllBankidFromSession(HttpSession session) {

		for (Enumeration<String> names = session.getAttributeNames(); names.hasMoreElements();) {
			String name = names.nextElement();
			if (name.startsWith("bankid.")) {
				session.removeAttribute(name);
			}
		}
	}

}
