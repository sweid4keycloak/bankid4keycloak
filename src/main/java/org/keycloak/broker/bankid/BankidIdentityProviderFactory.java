package org.keycloak.broker.bankid;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.HttpClient;
import org.keycloak.broker.bankid.client.SimpleBankidClient;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.connections.httpclient.HttpClientBuilder;
import org.keycloak.connections.httpclient.ProxyMappings;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class BankidIdentityProviderFactory extends AbstractIdentityProviderFactory	<BankidIdentityProvider> {

	public static final String PROVIDER_ID = "bankid";
	private static final Map<String, SimpleBankidClient> CLIENTS = new ConcurrentHashMap<>();
	
	@Override
	public String getName() {
		return "BankID e-legitimation";
	}

	@Override
	public BankidIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
		BankidIdentityProviderConfig config = new BankidIdentityProviderConfig(model);
		SimpleBankidClient client = CLIENTS.computeIfAbsent(clientKey(config),
				key -> new SimpleBankidClient(buildBankidHttpClient(config), config.getApiUrl()));
		return new BankidIdentityProvider(session, config, client);
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

	private String clientKey(BankidIdentityProviderConfig config) {
		String alias = config.getAlias() != null ? config.getAlias() : "";
		String apiUrl = config.getApiUrl() != null ? config.getApiUrl() : "";
		return alias + "@" + apiUrl;
	}

	private HttpClient buildBankidHttpClient(BankidIdentityProviderConfig config) {
		try {
			return (new HttpClientBuilder()).keyStore(config.getKeyStore(), config.getPrivateKeyPassword())
					.trustStore(config.getTrustStore())
					.proxyMappings(generateProxyMapping())
					.build();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create BankID HTTP Client", e);
		}
	}

	private ProxyMappings generateProxyMapping() {
		String httpsProxy = System.getenv("HTTPS_PROXY");
		if (httpsProxy == null) {
			httpsProxy = System.getenv("https_proxy");
		}

		String noProxy = System.getenv("NO_PROXY");
		if (noProxy == null) {
			noProxy = System.getenv("no_proxy");
		}

		return ProxyMappings.withFixedProxyMapping(httpsProxy, noProxy);
	}

}
