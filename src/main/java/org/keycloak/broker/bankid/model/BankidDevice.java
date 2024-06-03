package org.keycloak.broker.bankid.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankidDevice {
	private String ipAddress;
	private String uhi;
}
