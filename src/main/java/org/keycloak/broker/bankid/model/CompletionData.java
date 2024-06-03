package org.keycloak.broker.bankid.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompletionData {
	private BankidUser user;
	private BankidDevice device;
	private String bankIdIssueDate;
	private StepUp stepUp;
	private String signature;
	private String ocspResponse;
}
