package org.keycloak.broker.bankid.model;

import org.infinispan.protostream.annotations.ProtoField;

public class AuthResponse {

	@ProtoField(number = 1)
	String orderRef;

	@ProtoField(number = 2)
	String autoStartToken;

	@ProtoField(number = 3)
	String qrStartToken;

	@ProtoField(number = 4)
	String qrStartSecret;

	@ProtoField(number = 5)
	Long authTimestamp;

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

	public void setAuthTimestamp(Long authTimestamp) {
		this.authTimestamp = authTimestamp;
	}

	public Long getAuthTimestamp() {
		return authTimestamp;
	}
}
