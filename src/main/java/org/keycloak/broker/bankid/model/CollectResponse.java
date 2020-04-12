package org.keycloak.broker.bankid.model;

public class CollectResponse {
	
	String orderRef;
	String status;
	CompletionData completionData;
	
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
}
