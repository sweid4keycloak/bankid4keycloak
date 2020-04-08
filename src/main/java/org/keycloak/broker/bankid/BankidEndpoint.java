package org.keycloak.broker.bankid;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.RealmModel;

import com.fasterxml.jackson.databind.JsonNode;

public class BankidEndpoint {

	private RealmModel realm;
	private BankidIdentityProviderConfig config;
	private AuthenticationCallback callback;
	private BankidIdentityProvider provider;

	public BankidEndpoint(RealmModel realm, BankidIdentityProvider provider,
			BankidIdentityProviderConfig config, AuthenticationCallback callback) {
		this.realm = realm;
        this.config = config;
        this.callback = callback;
        this.provider = provider;
	}
	

	@GET
	@Path("/start")
	public Response start(@QueryParam("state") String state) {
//		 LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);
//		 return loginFormsProvider.createForm("start-bankid.html");
		return Response.ok(
				this.getClass().getClassLoader().getResourceAsStream("theme-resources/resources/start-bankid.html"),
				MediaType.TEXT_HTML_TYPE)
				.build();
	}
	
	@GET
	@Path("/logo")
	public Response logo() {
//		 LoginFormsProvider loginFormsProvider = provider.getSession().getProvider(LoginFormsProvider.class);
//		 return loginFormsProvider.createForm("start-bankid.html");
		return Response.ok(
				this.getClass().getClassLoader().getResourceAsStream("theme-resources/resources/bankid_vector_rgb.svg"),
				"image/svg+xml")
				.build();
	}
	
	@GET
	@Path("/login")
	public Response login(@QueryParam("nin") String nin,
			@QueryParam("state") String state) {
		return Response.ok(
				this.getClass().getClassLoader().getResourceAsStream("theme-resources/resources/login-bankid.html"),
				MediaType.TEXT_HTML_TYPE)
				.build();
	}
	
	@GET
	@Path("/collect")
	public Response login(@QueryParam("orderref") String orderref) {
		// TODO: Get info from BankId to know what we should return
		
    	return Response.ok(
				"{ \"status\": \"pending\" }",
				MediaType.APPLICATION_JSON_TYPE)
				.build();
	}
	
	@GET
	@Path("/done")
	public Response done(@QueryParam("state") String state,
			@QueryParam("orderref") String orderref) {
		// TODO: Get info from BankId
		String nin = "1111";
		BrokeredIdentityContext identity = new BrokeredIdentityContext(nin);
    	
    	identity.setIdpConfig(config);
    	identity.setIdp(provider);
    	identity.setUsername(nin);
    	identity.setFirstName("Joe");
    	identity.setLastName("Doe");
    	identity.setCode(state);

    	return callback.authenticated(identity);
    }
	
	public BankidIdentityProviderConfig getConfig() {
		return config;
	}

}
