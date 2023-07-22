package org.keycloak.broker.bankid;

import java.util.List;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class BankidIdentityProviderFactory extends AbstractIdentityProviderFactory	<BankidIdentityProvider> {

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
        return ProviderConfigurationBuilder.create()
				.property().name(BankidIdentityProviderConfig.BANKID_APIURL_PROPERTY_NAME).label("BankID API base URL").helpText("The base URL for the BankID API. Without the trailing slash. Normally https://appapi2.bankid.com")
                .type(ProviderConfigProperty.STRING_TYPE).add()

				.property().name(BankidIdentityProviderConfig.BANKID_KEYSTORE_FILE_PROPERTY_NAME).label("Keystore file").helpText("Full path to the keystore file, that holds the client certificate, including filename i.e. /path/to/file/myfile.p12")
                .type(ProviderConfigProperty.STRING_TYPE).add()
				
				.property().name(BankidIdentityProviderConfig.BANKID_KEYSTORE_PASSWORD_PROPERTY_NAME).label("Keystore password").helpText("The password for the keystore.")
                .type(ProviderConfigProperty.PASSWORD).add()
				
				.property().name(BankidIdentityProviderConfig.BANKID_PRIVATEKEY_PASSWORD_PROPERTY_NAME).label("Password for the private key").helpText("Password for the private key.")
                .type(ProviderConfigProperty.PASSWORD).add()
				
				.property().name(BankidIdentityProviderConfig.BANKID_TRUSTSTORE_FILE_PROPERTY_NAME).label("Truststore file").helpText("Full path to the truststore file, that holds the CA for the server certificate, including filename i.e. /path/to/file/bankid-truststore.p12")
                .type(ProviderConfigProperty.STRING_TYPE).add()
				
				.property().name(BankidIdentityProviderConfig.BANKID_TRUSTSTORE_PASSWORD_PROPERTY_NAME).label("Truststore password").helpText("The password for the truststore.")
                .type(ProviderConfigProperty.PASSWORD).add()
				
				.property().name(BankidIdentityProviderConfig.BANKID_REQUIRE_NIN).label("Require Personal Number").helpText("Require the user to provide their Personal Number before logging in.")
                .defaultValue(false)
				.type(ProviderConfigProperty.BOOLEAN_TYPE).add()

				.property().name(BankidIdentityProviderConfig.BANKID_SHOW_QR_CODE).label("Show QR code").helpText("Show QR code to allow user to use when starting the authentication.")
				.defaultValue(true)
                .type(ProviderConfigProperty.BOOLEAN_TYPE).add()

				.property().name(BankidIdentityProviderConfig.BANKID_SAVE_NIN_HASH).label("Use hashed Personal Number").helpText("Used hashed (SHA-256) Personal Number in keycloak instead of storing it in clear text.")
                .defaultValue(false)
                .type(ProviderConfigProperty.BOOLEAN_TYPE).add()
				
				.build();
    }

}
