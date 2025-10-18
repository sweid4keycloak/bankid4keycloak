package org.keycloak.broker.bankid;

import java.security.KeyStore;

import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.models.IdentityProviderModel;

public class BankidIdentityProviderConfig extends IdentityProviderModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3849007589404817838L;

	public static final String BANKID_APIURL_PROPERTY_NAME = "bankid_apiurl";
	public static final String BANKID_KEYSTORE_FILE_PROPERTY_NAME = "bankid_keystore_file";
	public static final String BANKID_KEYSTORE_PASSWORD_PROPERTY_NAME = "bankid_keystore_password";
	public static final String BANKID_TRUSTSTORE_FILE_PROPERTY_NAME = "bankid_truststore_file";
	public static final String BANKID_TRUSTSTORE_PASSWORD_PROPERTY_NAME = "bankid_truststore_password";
	public static final String BANKID_PRIVATEKEY_PASSWORD_PROPERTY_NAME = "bankid_privatekey_password";
	public static final String BANKID_REQUIRE_NIN = "bankid_require_nin";
	public static final String BANKID_SHOW_QR_CODE = "bankid_show_qr_code";
	public static final String BANKID_SAVE_NIN_HASH = "bankid_save_nin_hash";
	public static final String BANKID_CONNECTION_POOL_SIZE = "bankid_connection_pool_size";
	public static final String BANKID_MAX_POOLED_PER_ROUTE = "bankid_max_pooled_per_route";

	private KeyStore keyStore;
	private KeyStore truststore;

	public BankidIdentityProviderConfig() {
		super();
	}

	public BankidIdentityProviderConfig(IdentityProviderModel model) {
		super(model);
	}

	public String getApiUrl() {
		return getConfig().get(BANKID_APIURL_PROPERTY_NAME);
	}

	public void setApiUrl(final String apiUrl) {
		getConfig().put(BANKID_APIURL_PROPERTY_NAME, apiUrl);
	}

	public KeyStore getKeyStore() throws Exception {
		if (keyStore == null) {
			keyStore = KeystoreUtil.loadKeyStore(
					getConfig().get(BANKID_KEYSTORE_FILE_PROPERTY_NAME),
					getConfig().getOrDefault(BANKID_KEYSTORE_PASSWORD_PROPERTY_NAME, "changeit"));
		}
		return keyStore;
	}

	public KeyStore getTrustStore() throws Exception {
		if (truststore == null) {
			truststore = KeystoreUtil.loadKeyStore(
					getConfig().get(BANKID_TRUSTSTORE_FILE_PROPERTY_NAME),
					getConfig().getOrDefault(BANKID_TRUSTSTORE_PASSWORD_PROPERTY_NAME, "changeit"));
		}
		return truststore;
	}

	public String getPrivateKeyPassword() {
		return getConfig().getOrDefault(BANKID_PRIVATEKEY_PASSWORD_PROPERTY_NAME, "changeit");
	}

	public boolean isShowQRCode() {
		return Boolean.valueOf(getConfig().getOrDefault(BANKID_SHOW_QR_CODE, "false"));
	}

	public boolean isRequiredNin() {
		return Boolean.valueOf(getConfig().getOrDefault(BANKID_REQUIRE_NIN, "false"));
	}

	public void setRequiredNin(boolean requiredNin) {
		getConfig().put(BANKID_REQUIRE_NIN, String.valueOf(requiredNin));
	}


	public boolean isSaveNinHashed() {
		return Boolean.valueOf(getConfig().getOrDefault(BANKID_SAVE_NIN_HASH, "false"));
	}

	public int getConnectionPoolSize() {
		return parsePositiveInt(BANKID_CONNECTION_POOL_SIZE, 200);
	}

	public int getConnectionPoolPerRoute() {
		int value = parsePositiveInt(BANKID_MAX_POOLED_PER_ROUTE, 50);
		int total = getConnectionPoolSize();
		return value > total ? total : value;
	}

	private int parsePositiveInt(String key, int defaultValue) {
		String raw = getConfig().get(key);
		if (raw == null || raw.trim().isEmpty()) {
			return defaultValue;
		}

		try {
			int parsed = Integer.parseInt(raw.trim());
			return parsed > 0 ? parsed : defaultValue;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}
