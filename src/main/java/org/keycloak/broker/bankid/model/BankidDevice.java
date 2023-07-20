package org.keycloak.broker.bankid.model;

public class BankidDevice {
	String ipAddress;
	String uhi;

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUhi() {
		return uhi;
	}

	public void setUhi(String uhi) {
		this.uhi = uhi;
	}
}
