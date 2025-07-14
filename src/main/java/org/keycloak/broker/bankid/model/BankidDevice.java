package org.keycloak.broker.bankid.model;

import org.infinispan.protostream.annotations.ProtoField;

public class BankidDevice {

	@ProtoField(number = 1)
	String ipAddress;

	@ProtoField(number = 2)
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
