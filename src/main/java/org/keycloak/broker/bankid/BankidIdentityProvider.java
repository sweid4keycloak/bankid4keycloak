package org.keycloak.broker.bankid;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.apache.http.client.HttpClient;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.connections.httpclient.HttpClientBuilder;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public class BankidIdentityProvider extends AbstractIdentityProvider<BankidIdentityProviderConfig> {

	public BankidIdentityProvider(KeycloakSession session, BankidIdentityProviderConfig config) {
		super(session, config);
	}
	
	@Override
	public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
		return new BankidEndpoint(this, getConfig(), callback);
	}
	
	@Override
	public Response performLogin(AuthenticationRequest request) {
		try {
			return Response.temporaryRedirect(
					new URI(request.getRedirectUri() 
							+ "/start?state="+ request.getState().getEncoded()))
					.build();
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
	
	public HttpClient buildBankidHttpClient() throws Exception {
		
		return (new HttpClientBuilder())
				.keyStore(getConfig().getKeyStore(), getConfig().getPrivateKeyPassword())
				.trustStore(KeystoreUtil.loadKeyStore("", ""))
				.build();
	}

}
