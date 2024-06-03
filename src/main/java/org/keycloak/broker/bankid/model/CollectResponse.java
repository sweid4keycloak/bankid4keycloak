package org.keycloak.broker.bankid.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectResponse {
	private String orderRef;
	private String status;
	private CompletionData completionData;
	private String errorCode;
	private String details;
	private String hintCode;
}
