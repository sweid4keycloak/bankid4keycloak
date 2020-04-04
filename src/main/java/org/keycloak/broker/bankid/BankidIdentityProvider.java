package org.keycloak.broker.bankid;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import com.fasterxml.jackson.databind.JsonNode;

public class BankidIdentityProvider extends AbstractIdentityProvider<BankidIdentityProviderConfig> {

	public BankidIdentityProvider(KeycloakSession session, BankidIdentityProviderConfig config) {
		super(session, config);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
		return new BankidEndpoint(realm, this, getConfig(), callback);
	}
	
	@Override
	public Response performLogin(AuthenticationRequest request) {
		try {
			JsonNode json = SimpleHttp.doGet(getConfig().getBaseServiceUrl()+"/new/shuffle/?deck_count=1", request.getSession())
				.acceptJson()
				.asJson();
			String deckId = json.get("deck_id").asText();
			return Response.temporaryRedirect(
					new URI(request.getRedirectUri() 
							+ "?deckId="+deckId
							+ "&state="+ request.getState().getEncoded()))
					.build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public KeycloakSession getSession() {
		return this.session;
	}

	@Override
	public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity) {
		return Response.ok(identity.getToken()).build();
	}

}
