package org.keycloak.broker.bankid.model;

public class AuthResponse {

	String orderRef;
	String autoStartToken;
	String qrStartToken;
	String qrStartSecret;
	long authTimestamp;

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

	public void setQrStartToken(String qrStartToken) {
		this.qrStartToken = qrStartToken;
	}

	public String getQrStartToken() {
		return qrStartToken;
	}

	public void setQrStartSecret(String qrStartSecret) {
		this.qrStartSecret = qrStartSecret;
	}

	public String getQrStartSecret() {
		return qrStartSecret;
	}

	public void setAuthTimestamp(long authTimestamp) {
		this.authTimestamp = authTimestamp;
	}

	public long getAuthTimestamp() {
		return authTimestamp;
	}
}
