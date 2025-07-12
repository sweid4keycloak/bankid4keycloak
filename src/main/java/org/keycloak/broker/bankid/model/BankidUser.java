package org.keycloak.broker.bankid.model;

import org.infinispan.protostream.annotations.ProtoField;

public class BankidUser {

	@ProtoField(number = 1)
	String personalNumber;

	@ProtoField(number = 2)
	String name;

	@ProtoField(number = 3)
	String givenName;

	@ProtoField(number = 4)
	String surname;

	public String getPersonalNumber() {
		return personalNumber;
	}

	public void setPersonalNumber(String personalNumber) {
		this.personalNumber = personalNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}
}
