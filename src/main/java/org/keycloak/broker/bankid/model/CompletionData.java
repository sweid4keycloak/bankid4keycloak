package org.keycloak.broker.bankid.model;

import org.infinispan.protostream.annotations.ProtoField;

public class CompletionData {

	@ProtoField(number = 1)
	BankidUser user;

	@ProtoField(number = 2)
	BankidDevice device;

	@ProtoField(number = 3)
	String bankIdIssueDate;

	@ProtoField(number = 4)
	StepUp stepUp;

	@ProtoField(number = 5)
	String signature;

	@ProtoField(number = 6)
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

	public String getBankIdIssueDate() {
		return bankIdIssueDate;
	}

	public void setBankIdIssueDate(String bankIdIssueDate) {
		this.bankIdIssueDate = bankIdIssueDate;
	}

	public StepUp getStepUp() {
		return stepUp;
	}

	public void setStepUp(StepUp stepUp) {
		this.stepUp = stepUp;
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
