package org.keycloak.broker.bankid;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
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
    public Response redirectBinding(@QueryParam("deckId") String deckId,
    		@QueryParam("state") String relayState)  {
    	try {
			JsonNode json = SimpleHttp.doGet(getConfig().getBaseServiceUrl()+"/"+deckId+"/draw/?count=1", provider.getSession())
				.acceptJson()
				.asJson();
			JsonNode card = json.get("cards").get(0);
	    	BrokeredIdentityContext identity = new BrokeredIdentityContext(card.get("code").asText());
	    	identity.setIdpConfig(config);
	    	identity.setIdp(provider);
	    	identity.setCode(relayState);
	    	identity.setUsername(card.get("code").asText());
	    	identity.setFirstName(card.get("value").asText());
	    	identity.setLastName(card.get("suit").asText());

	    	return callback.authenticated(identity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return callback.error("500", "DOH!");
    }
	
	public BankidIdentityProviderConfig getConfig() {
		return config;
	}

}
