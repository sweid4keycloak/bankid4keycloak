package org.keycloak.lf.userprofileauthenticator.model;

public enum UserServiceHindCodes {
// Internal error (by the BankID IDP Provider)
	internal("USER_SERVICE_ERROR"),
	userservice500("USER_SERVICE_ERROR_500"),
	userservice400("USER_SERVICE_ERROR_400");

	public final String messageShortName;

	private UserServiceHindCodes(String messageShortName) {
		this.messageShortName = messageShortName;
	}

}
