package org.keycloak.broker.bankid.model;

public enum BankidHintCodes {
	// Pending
	outstandingTransaction,
	noClient,
	started,
	userSign,
	
	// 400 Error codes 
	alreadyInProgress,
	invalidParameters,
	
	// 503 Error codes 
	Maintenance,
	
	// Errors
	expiredTransaction,
	certificateErr,
	userCancel,
	cancelled,
	startFailed,
	unkown,
	
	// Internal error (by the BankID IDP Provider)
	internal

}
