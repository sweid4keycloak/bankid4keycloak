package org.keycloak.broker.bankid.client;

import org.keycloak.broker.bankid.model.BankidHintCodes;

public class BankidClientException extends RuntimeException {

	private static final long serialVersionUID = 2731514638312541944L;

	private BankidHintCodes hintCode;

	public BankidClientException(BankidHintCodes hintcode) {
		this(hintcode, null);
	}

	public BankidClientException(BankidHintCodes hintcode, Throwable cause) {
		super(cause);
		this.hintCode = hintcode;
	}

	public BankidHintCodes getHintCode() {
		return hintCode;
	}
}
