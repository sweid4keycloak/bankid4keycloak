package org.keycloak.broker.bankid;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.client.HttpClient;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.connections.httpclient.HttpClientBuilder;
import org.keycloak.connections.httpclient.ProxyMappings;
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
			return Response.status(302)
					.location(new URI(request.getRedirectUri() + "/start?state=" + request.getState().getEncoded()))
					.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException();
		}
	}

	public KeycloakSession getSession() {
		return this.session;
	}

	@Override
	public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity) {
		return Response.ok(identity.getToken()).build();
	}

	public ProxyMappings generateProxyMapping(){
		String httpsProxy = System.getenv("HTTPS_PROXY");
		if(httpsProxy == null){
			httpsProxy = System.getenv("https_proxy");
		}

		String noProxy = System.getenv("NO_PROXY");
		if(noProxy == null){
			noProxy = System.getenv("no_proxy");
		}

		return ProxyMappings.withFixedProxyMapping(httpsProxy, noProxy);
	}

	public UriBuilder redirectUriBuilder() {
		return session.getContext().getUri().getBaseUriBuilder()
			.path("realms")
			.path(session.getContext().getRealm().getId())
			.path("broker")
			.path(getConfig().getAlias())
			.path("endpoint");
	}

	public HttpClient buildBankidHttpClient() {

		try {
			return (new HttpClientBuilder()).keyStore(getConfig().getKeyStore(), getConfig().getPrivateKeyPassword())
					.trustStore(getConfig().getTrustStore())
					.proxyMappings(generateProxyMapping())
					.build();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create BankID HTTP Client", e);
		}
	}

}
