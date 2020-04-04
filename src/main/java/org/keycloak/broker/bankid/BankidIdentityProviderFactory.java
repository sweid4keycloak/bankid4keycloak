package org.keycloak.broker.bankid;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

public class BankidIdentityProviderFactory extends AbstractIdentityProviderFactory<BankidIdentityProvider> {

	public static final String PROVIDER_ID = "bankid";
	
	@Override
	public String getName() {
		return "BankID e-legitimation";
	}

	@Override
	public BankidIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
		return new BankidIdentityProvider(session, new BankidIdentityProviderConfig(model));
	}

	@SuppressWarnings("unchecked")
	@Override
	public BankidIdentityProviderConfig createConfig() {
		return new BankidIdentityProviderConfig();
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

}
