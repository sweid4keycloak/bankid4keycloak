package org.keycloak.lf.userprofileauthenticator.model;

public class UserResponse {
	private String userId;
    private String personalNumber;
    private String firstName;
    private String lastName; 
    private Boolean isProtectedIdentity;
    private Boolean isInternal;
    public String getUserId() {
		return userId;
	}
    public void setUserId(String userId) {
		this.userId = userId;
	}
    public String getPersonalNumber() {
		return personalNumber;
	}
    public void setPersonalNumber(String personalNumber) {
		this.personalNumber = personalNumber;
	}
    public String getFirstName() {
		return firstName;
	}
    public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
    public String getLastName() {
		return lastName;
	}
    public void setLastName(String lastName) {
		
}
	/**
	 * @return the isProtectedIdentity
	 */
	public Boolean getIsProtectedIdentity() {
		return isProtectedIdentity;
	}
	/**
	 * @param isProtectedIdentity the isProtectedIdentity to set
	 */
	public void setIsProtectedIdentity(Boolean isProtectedIdentity) {
		this.isProtectedIdentity = isProtectedIdentity;
	}
	/**
	 * @return the isInternal
	 */
	public Boolean getIsInternal() {
		return isInternal;
	}
	/**
	 * @param isInternal the isInternal to set
	 */
	public void setIsInternal(Boolean isInternal) {
		this.isInternal = isInternal;
	}
}