package org.keycloak.broker.bankid;

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
import org.keycloak.broker.bankid.model.BankidHintCodes;
import org.keycloak.broker.bankid.model.BankidUser;
import org.keycloak.broker.bankid.model.CollectResponse;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;
import org.keycloak.forms.login.LoginFormsProvider;

public class BankidEndpoint {

	private BankidIdentityProviderConfig config;
	private AuthenticationCallback callback;
	private BankidIdentityProvider provider;
	private SimpleBankidClient bankidClient;
    private static final Logger logger = Logger.getLogger(BankidEndpoint.class);

	
	public BankidEndpoint(BankidIdentityProvider provider,
			BankidIdentityProviderConfig config, AuthenticationCallback callback) {
	    this.config = config;
        this.callback = callback;
        this.provider = provider;
        this.bankidClient = new SimpleBankidClient(provider.buildBankidHttpClient(), config.getApiUrl());
        
	}
	

	@GET
	@Path("/start")
	public Response start(@QueryParam("state") String state,
			@Context HttpServletRequest request) {
		
		state = getOrSetStringFromSession(request.getSession(), "bankid.state", state);
		
		if ( state == null ) {
			return callback.error(state, "bankid.hints." +BankidHintCodes.internal.messageShortName);
		}
		LoginFormsProvider loginFormsProvider 
			= provider.getSession().getProvider(LoginFormsProvider.class);
		return loginFormsProvider
				.createForm("start-bankid.ftl")
				;
	}

	@POST
	@Path("/login")
	public Response login(@FormParam("nin") String nin,
			@Context HttpServletRequest request) {
		LoginFormsProvider loginFormsProvider 
			= provider.getSession().getProvider(LoginFormsProvider.class);

		String state = getOrSetStringFromSession(request.getSession(), "bankid.state", null);
		if ( state == null ) {
			return callback.error(state, "bankid.hints." +BankidHintCodes.internal.messageShortName);
		}
		try {
			if ( request.getSession().getAttribute("orderref") == null ) {
				request.getSession().setAttribute("orderref", 
					bankidClient.sendAuth(nin, request.getRemoteAddr()));
			}
			
			return loginFormsProvider
					.setAttribute("state", state)
					.createForm("login-bankid.ftl");
		} catch (BankidClientException e) {
			return loginFormsProvider
						.setError("bankid.hints." + e.getHintCode().messageShortName)
						.createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("/collect")
	public Response collect(@Context HttpServletRequest request) {
		if ( request.getSession().getAttribute("orderref") != null ) {
			String orderref = request.getSession().getAttribute("orderref").toString();
			try {
				CollectResponse responseData = bankidClient.sendCollect(orderref) ;
				// Check responseData.getStatus()
				if ( "failed".equalsIgnoreCase(responseData.getStatus()) ) {
					return Response.status(Status.INTERNAL_SERVER_ERROR)
							.entity(
									String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }", 
									responseData.getStatus(), responseData.getHintCode()))
							.type(MediaType.APPLICATION_JSON_TYPE)
							.build();
				} else {
					if ( "complete".equalsIgnoreCase(responseData.getStatus()) ) {
						request.getSession().removeAttribute("orderref");
						request.getSession().setAttribute("bankidUser",responseData.getCompletionData().getUser());
					} 
					return Response.ok(
							String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }", 
									responseData.getStatus(), responseData.getHintCode()),
							MediaType.APPLICATION_JSON_TYPE)
							.build();
				}
			} catch (BankidClientException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(
								String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }", 
								"failed", e.getHintCode()))
						.type(MediaType.APPLICATION_JSON_TYPE)
						.build();
			}
		} else {
			return Response.ok(
					String.format("{ \"status\": \"%s\", \"hintCode\": \"%s\" }", 
							"500", "internal"),
					MediaType.APPLICATION_JSON_TYPE)
					.build();
		}
	}
	
	@GET
	@Path("/done")
	public Response done(@Context HttpServletRequest request) {

		String state = getOrSetStringFromSession(request.getSession(), "bankid.state", null);
		if ( state == null ) {
			request.getSession().removeAttribute("orderref");
			return callback.error(state, "bankid.hints." +BankidHintCodes.internal.messageShortName);
		}
		
		LoginFormsProvider loginFormsProvider 
			= provider.getSession().getProvider(LoginFormsProvider.class);
		
		// Make sure to remove the orderref attribute from the session
		request.getSession().removeAttribute("orderref");
		
		if ( request.getSession().getAttribute("bankidUser") == null 
				|| !(request.getSession().getAttribute("bankidUser") instanceof BankidUser) ) {
			logger.error("Session attribute 'bankidUser' not set or not correct type.");
			return loginFormsProvider
					.setError("bankid.error.internal")
					.createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}
		BankidUser user = (BankidUser) request.getSession().getAttribute("bankidUser");
		try {
			BrokeredIdentityContext identity = new BrokeredIdentityContext(user.getPersonalNumber());
	    	
			identity.setIdpConfig(config);
	    	identity.setIdp(provider);
	    	identity.setUsername(user.getPersonalNumber());
	    	identity.setFirstName(user.getGivenName());
	    	identity.setLastName(user.getSurname());
	    	identity.setCode(state);

	    	return callback.authenticated(identity);
		} catch (Exception e) {
			throw new RuntimeException("Failed to call BankID", e);
		}
    }

	@GET
	@Path("/cancel")
	public Response canel(@Context HttpServletRequest request) {

		String state = getOrSetStringFromSession(request.getSession(), "bankid.state", null);
		if ( state == null ) {
			request.getSession().removeAttribute("orderref");
			return callback.error(state, "bankid.hints." +BankidHintCodes.internal.messageShortName);
		}
		String orderRef = request.getSession().getAttribute("orderref").toString();
		bankidClient.sendCancel(orderRef);
		// Make sure to remove the orderref attribute from the session
		request.getSession().removeAttribute("orderref");
		
		return callback.error(state, "bankid.hints." +BankidHintCodes.cancelled.messageShortName);
    }
	
	@GET
	@Path("/error")
	public Response error(@QueryParam("code") String hintCode,
			@Context HttpServletRequest request) {
		// Make sure to remove the orderref attribute from the session
		request.getSession().removeAttribute("orderref");

		BankidHintCodes hint;
		// Sanitize input from the web
		try {
			hint = BankidHintCodes.valueOf(hintCode);
		} catch (IllegalArgumentException e) {
			hint = BankidHintCodes.unkown;
		}
		LoginFormsProvider loginFormsProvider 
			= provider.getSession().getProvider(LoginFormsProvider.class);
		return loginFormsProvider
				.setError("bankid.hints." + hint.messageShortName)
				.createErrorPage(Status.INTERNAL_SERVER_ERROR);
    }

	public BankidIdentityProviderConfig getConfig() {
		return config;
	}
	
	private String getOrSetStringFromSession(HttpSession session, String name, String defaultValue) {	
		if ( session.getAttribute(name) == null) {
			session.setAttribute(name, defaultValue);
		}
		Object value = session.getAttribute(name);
		return (value==null?null:value.toString());
	}

}
