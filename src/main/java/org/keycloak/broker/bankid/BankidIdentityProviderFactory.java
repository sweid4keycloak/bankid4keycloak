package org.keycloak.broker.bankid;

import java.util.List;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class BankidIdentityProviderFactory extends AbstractIdentityProviderFactory<BankidIdentityProvider> implements SocialIdentityProviderFactory<BankidIdentityProvider> {

	public static final String PROVIDER_ID = "bankid";
	
	@Override
	public String getName() {
		return "BankID e-legitimation";
	}

	@Override
	public BankidIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
		return new BankidIdentityProvider(session, new BankidIdentityProviderConfig(model));
	}

	@Override
	public BankidIdentityProviderConfig createConfig() {
		return new BankidIdentityProviderConfig();
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create().property()
                .name("apiUrl").label("BankID API base URL").helpText("The base URL for the BankID API. Without the trailing slash. Normally https://appapi2.bankid.com")
                .type(ProviderConfigProperty.STRING_TYPE).add()
				.property().name("requiredNin").label("Require Personal Number").helpText("Require the user to provide their Personal Number before logging in.")
                .type(ProviderConfigProperty.BOOLEAN_TYPE).add()
				.build();
    }

}
