package org.keycloak.broker.bankid.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
	private String orderRef;
	private String autoStartToken;
	private String qrStartToken;
	private String qrStartSecret;
	private long authTimestamp;
}
