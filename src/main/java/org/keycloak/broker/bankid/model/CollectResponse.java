package org.keycloak.broker.bankid.model;

public class CollectResponse {

	String orderRef;
	String status;
	CompletionData completionData;
	String errorCode;
	String details;
	String hintCode;

	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public CompletionData getCompletionData() {
		return completionData;
	}

	public void setCompletionData(CompletionData completionData) {
		this.completionData = completionData;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getHintCode() {
		return hintCode;
	}

	public void setHintCode(String hintCode) {
		this.hintCode = hintCode;
	}
}
