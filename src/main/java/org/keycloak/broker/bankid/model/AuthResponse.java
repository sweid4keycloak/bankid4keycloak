package org.keycloak.broker.bankid.model;

public class AuthResponse {
	
	String orderRef;
	String autoStartToken;
	
	public void setAutoStartToken(String autoStartToken) {
		this.autoStartToken = autoStartToken;
	}
	public String getAutoStartToken() {
		return autoStartToken;
	}
	
	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}
	public String getOrderRef() {
		return orderRef;
	}

}
