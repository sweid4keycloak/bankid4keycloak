package org.keycloak.broker.bankid;

import java.security.KeyStore;

import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.models.IdentityProviderModel;

public class BankidIdentityProviderConfig extends IdentityProviderModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3849007589404817838L;
	
	private static final String BANKID_HOST_PROPERTY_NAME = "bankid_hostname";
	private static final String BANKID_KEYSTORE_FILE_PROPERTY_NAME = "bankid_keystore_file";
	private static final String BANKID_KEYSTORE_PASSWORD_PROPERTY_NAME = "bankid_keystore_password";
	private static final String BANKID_TRUSTSTORE_FILE_PROPERTY_NAME = "bankid_truststore_file";
	private static final String BANKID_TRUSTSTORE_PASSWORD_PROPERTY_NAME = "bankid_truststore_password";
	private static final String BANKID_PRIVATEKEY_PASSWORD_PROPERTY_NAME = "bankid_privatekey_password";
	
	public BankidIdentityProviderConfig() {
		super();
	}
	
	public BankidIdentityProviderConfig(IdentityProviderModel model) {
		super(model);
	}

	public String getHostName() {
		return getConfig().get(BANKID_HOST_PROPERTY_NAME);
	}
	public KeyStore getKeyStore() throws Exception {
		return KeystoreUtil.loadKeyStore(
				getConfig().get(BANKID_KEYSTORE_FILE_PROPERTY_NAME), 
				getConfig().getOrDefault(BANKID_KEYSTORE_PASSWORD_PROPERTY_NAME, "changeit"));
	}
	public KeyStore getTrustStore() throws Exception {
		return KeystoreUtil.loadKeyStore(
				getConfig().get(BANKID_TRUSTSTORE_FILE_PROPERTY_NAME), 
				getConfig().getOrDefault(BANKID_TRUSTSTORE_PASSWORD_PROPERTY_NAME, "changeit"));
	}
	
	public String getPrivateKeyPassword() {
		return getConfig().getOrDefault(BANKID_PRIVATEKEY_PASSWORD_PROPERTY_NAME, "changeit");
	}
}
