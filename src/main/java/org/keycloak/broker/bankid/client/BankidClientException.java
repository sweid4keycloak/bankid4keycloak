package org.keycloak.broker.bankid.client;

import lombok.Getter;
import org.keycloak.broker.bankid.model.BankidHintCodes;

@Getter
public class BankidClientException extends RuntimeException {
	private final BankidHintCodes hintCode;

	public BankidClientException(BankidHintCodes hintcode) {
		this(hintcode, null);
	}

	public BankidClientException(BankidHintCodes hintcode, Throwable cause) {
		super(cause);
		this.hintCode = hintcode;
	}
}
