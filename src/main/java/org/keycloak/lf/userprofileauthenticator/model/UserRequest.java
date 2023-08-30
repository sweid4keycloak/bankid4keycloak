package org.keycloak.lf.userprofileauthenticator.model; 

public class UserRequest {
	
	private String userId;
    private String personalNumber;
    private String firstName;
    private String lastName; 
    
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
		this.lastName = lastName;
	} 

}
