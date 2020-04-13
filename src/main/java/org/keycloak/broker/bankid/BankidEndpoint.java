package org.keycloak.broker.bankid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.keycloak.broker.bankid.client.BankidClientException;
import org.keycloak.broker.bankid.client.SimpleBankidClient;
import org.keycloak.broker.bankid.model.CollectResponse;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.forms.login.LoginFormsProvider;

public class BankidEndpoint {

	private BankidIdentityProviderConfig config;
	private AuthenticationCallback callback;
	private BankidIdentityProvider provider;
	private SimpleBankidClient bankidClient;
	
	public BankidEndpoint(BankidIdentityProvider provider,
			BankidIdentityProviderConfig config, AuthenticationCallback callback) {
	    this.config = config;
        this.callback = callback;
        this.provider = provider;
        this.bankidClient = new SimpleBankidClient(provider.buildBankidHttpClient(), config.getApiUrl());
        
	}
	

	@GET
	@Path("/start")
	public Response start(@QueryParam("state") String state) {
		LoginFormsProvider loginFormsProvider 
			= provider.getSession().getProvider(LoginFormsProvider.class);
		return loginFormsProvider
				.setAttribute("state", state)
				.createForm("start-bankid.ftl")
				;
	}
	
	@GET
	@Path("/login")
	public Response login(@QueryParam("nin") String nin,
			@QueryParam("state") String state,
			@Context HttpServletRequest request) {
		LoginFormsProvider loginFormsProvider 
			= provider.getSession().getProvider(LoginFormsProvider.class);
	
		try {
			request.getSession().setAttribute("orderref", 
					bankidClient.sendAuth(nin, request.getRemoteAddr()));
				
			return loginFormsProvider
					.setAttribute("state", state)
					.createForm("login-bankid.ftl");
		} catch (BankidClientException e) {
			return loginFormsProvider
						.setError("bankid.error." + e.getHintCode())
						.createErrorPage(Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("/collect")
	public Response login(@Context HttpServletRequest request) {

		String orderref = request.getSession().getAttribute("orderref").toString();
		Map<String, String> requestData = new HashMap<>();
		requestData.put("orderRef", orderref);
		try {
			CollectResponse responseData = SimpleHttp.doPost(
					getConfig().getApiUrl() + "/rp/v5/collect", 
					provider.buildBankidHttpClient())
				.json(requestData)
				.asJson(CollectResponse.class);
			return Response.ok(
					String.format("{ \"status\": \"%s\" }", responseData.getStatus()),
					MediaType.APPLICATION_JSON_TYPE)
					.build();
		} catch (IOException e) {
			throw new RuntimeException("Failed to call BankID", e);
		} catch (Exception e) {
			throw new RuntimeException("Failed to call BankID", e);
		}
	}
	
	@GET
	@Path("/done")
	public Response done(@QueryParam("nin") String nin,
			@QueryParam("state") String state,
			@Context HttpServletRequest request) {
		String orderref = request.getSession().getAttribute("orderref").toString();
		Map<String, String> requestData = new HashMap<>();
		requestData.put("orderRef", orderref);
		try {
			CollectResponse responseData = SimpleHttp.doPost(
					getConfig().getApiUrl() + "/rp/v5/collect", 
					provider.buildBankidHttpClient())
				.json(requestData)
				.asJson(CollectResponse.class);
			
			// TODO: Check that status is "complete"
			
			BrokeredIdentityContext identity = new BrokeredIdentityContext(
					responseData.getCompletionData().getUser().getPersonalNumber());
	    	
	    	identity.setIdpConfig(config);
	    	identity.setIdp(provider);
	    	identity.setUsername(responseData.getCompletionData().getUser().getPersonalNumber());
	    	identity.setFirstName(responseData.getCompletionData().getUser().getGivenName());
	    	identity.setLastName(responseData.getCompletionData().getUser().getSurname());
	    	identity.setCode(state);

	    	return callback.authenticated(identity);
		} catch (IOException e) {
			throw new RuntimeException("Failed to call BankID", e);
		} catch (Exception e) {
			throw new RuntimeException("Failed to call BankID", e);
		}
    }

	@GET
	@Path("/cancel")
	public Response canel(@QueryParam("state") String state,
			@Context HttpServletRequest request) {
		// String orderref = request.getSession().getAttribute("orderref").toString();
		// TODO: Send cancel to bankid
		return callback.cancelled(state);
    }

	public BankidIdentityProviderConfig getConfig() {
		return config;
	}
}
