package org.keycloak.broker.bankid;

import org.keycloak.models.IdentityProviderModel;

public class BankidIdentityProviderConfig extends IdentityProviderModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3849007589404817838L;
	
	private String baseServiceUrl = "https://deckofcardsapi.com/api/deck/";
	
	public BankidIdentityProviderConfig() {
		super();
	}

	public BankidIdentityProviderConfig(IdentityProviderModel model) {
		super(model);
	}
	
	public String getBaseServiceUrl() {
		return baseServiceUrl;
	}
	public void setBaseServiceUrl(String baseServiceUrl) {
		this.baseServiceUrl = baseServiceUrl;
	}
}
