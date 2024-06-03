package org.keycloak.broker.bankid.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankidUser {
	private String personalNumber;
	private String name;
	private String givenName;
	private String surname;
}
