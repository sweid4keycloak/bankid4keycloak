package org.keycloak.broker.bankid.model;

public class CompletionData {
	BankidUser user;
	BankidDevice device;
	BankidCert cert;
	String signature;
	String ocspResponse;

	public BankidUser getUser() {
		return user;
	}

	public void setUser(BankidUser user) {
		this.user = user;
	}

	public BankidDevice getDevice() {
		return device;
	}

	public void setDevice(BankidDevice device) {
		this.device = device;
	}

	public BankidCert getCert() {
		return cert;
	}

	public void setCert(BankidCert cert) {
		this.cert = cert;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getOcspResponse() {
		return ocspResponse;
	}

	public void setOcspResponse(String ocspResponse) {
		this.ocspResponse = ocspResponse;
	}

}
