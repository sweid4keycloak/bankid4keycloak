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

import org.keycloak.broker.bankid.model.CollectResponse;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;
import org.keycloak.broker.provider.util.SimpleHttp;

public class BankidEndpoint {

	private BankidIdentityProviderConfig config;
	private AuthenticationCallback callback;
	private BankidIdentityProvider provider;
	
	public BankidEndpoint(BankidIdentityProvider provider,
			BankidIdentityProviderConfig config, AuthenticationCallback callback) {
	    this.config = config;
        this.callback = callback;
        this.provider = provider;
	}
	

	@GET
	@Path("/start")
	public Response start(@QueryParam("state") String state) {
		return Response.ok(
				this.getClass().getClassLoader().getResourceAsStream("theme-resources/resources/start-bankid.html"),
				MediaType.TEXT_HTML_TYPE)
				.build();
	}
	
	@GET
	@Path("/logo")
	public Response logo() {
		return Response.ok(
				this.getClass().getClassLoader().getResourceAsStream("theme-resources/resources/bankid_vector_rgb.svg"),
				"image/svg+xml")
				.build();
	}
	
	@GET
	@Path("/login")
	public Response login(@QueryParam("nin") String nin,
			@QueryParam("state") String state,
			@Context HttpServletRequest request) {
		
		Map<String, String> requestData = new HashMap<>();
		
		requestData.put("personalNumber", nin);
		requestData.put("endUserIp", request.getRemoteAddr());

		try {
			@SuppressWarnings("unchecked")
			Map<String, String> responseData = SimpleHttp.doPost(
					"https://" + getConfig().getHostName() + "/rp/v5/auth", 
					provider.buildBankidHttpClient())
				.json(requestData)
				.asJson(Map.class);
			request.getSession().setAttribute("orderref", responseData.get("orderRef"));
			
			return Response.ok(
					this.getClass().getClassLoader().getResourceAsStream("theme-resources/resources/login-bankid.html"),
					MediaType.TEXT_HTML_TYPE)
					.build();
		} catch (IOException e) {
			throw new RuntimeException("Failed to call BankID", e);
		} catch (Exception e) {
			throw new RuntimeException("Failed to call BankID", e);
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
					"https://" + getConfig().getHostName() + "/rp/v5/collect", 
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
					"https://" + getConfig().getHostName() + "/rp/v5/collect", 
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
	
	public BankidIdentityProviderConfig getConfig() {
		return config;
	}
}
