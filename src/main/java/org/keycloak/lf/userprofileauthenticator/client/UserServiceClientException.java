package org.keycloak.lf.userprofileauthenticator.client;
 
import org.keycloak.lf.userprofileauthenticator.model.UserServiceHindCodes;

public class UserServiceClientException extends RuntimeException {

	private static final long serialVersionUID = 2731514638312541988L;

	private UserServiceHindCodes hintCode;

	public UserServiceClientException(UserServiceHindCodes hintcode) {
		this(hintcode, null);
	}

	public UserServiceClientException(UserServiceHindCodes hintcode, Throwable cause) {
		super(cause);
		this.hintCode = hintcode;
	}

	public UserServiceHindCodes getHintCode() {
		return hintCode;
	}
}
