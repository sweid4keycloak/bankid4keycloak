package org.keycloak.lf.userprofileauthenticator;

import java.util.Map;

import org.apache.http.client.HttpClient;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.connections.httpclient.HttpClientBuilder; 
import org.keycloak.lf.userprofileauthenticator.client.UserServiceClient;
import org.keycloak.lf.userprofileauthenticator.client.UserServiceClientException;
import org.keycloak.lf.userprofileauthenticator.model.UserRequest;
import org.keycloak.lf.userprofileauthenticator.model.UserResponse;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class UserProfileAuthenticator  implements Authenticator {

    private static final Logger logger = Logger.getLogger(UserProfileAuthenticator.class);
    private static final String IS_USER_PROFILE_CREATED   = "isuserprofilecreated";
    private static final String IS_PROTECTE_IDENTIRY   = "isprotectedidentity"; 
    
    @Override
    public void authenticate(AuthenticationFlowContext context) { 
         UserModel user = context.getUser();
       
        String userServiceUrl = getUserServiceUrl(context);
        String apiKey = getUserServiceApiKey(context);
        logger.error(user.getUsername());
        logger.error(user.getId());
        Boolean isUserProfileCreated = Boolean.valueOf(user.getFirstAttribute(IS_USER_PROFILE_CREATED)) ;
        logger.error(userServiceUrl);
        logger.error(isUserProfileCreated);
        UserServiceClient userServiceClient  = null;
        if(!isUserProfileCreated) {
        	try {
	        	userServiceClient = new  UserServiceClient(buildHttpClient() , userServiceUrl, apiKey);
	        	UserRequest request = new UserRequest();
	        	request.setUserId(user.getId());
	        	request.setFirstName(user.getFirstName());
	        	request.setLastName(user.getLastName());
	        	request.setPersonalNumber(user.getUsername()); 
	        	UserResponse response = userServiceClient.sendAuth(request);
	        	user.setSingleAttribute(IS_USER_PROFILE_CREATED, "true");
	        	user.setSingleAttribute(IS_PROTECTE_IDENTIRY,response.getIsProtectedIdentity().toString());
	        	context.success();
        	} 
        	catch (UserServiceClientException e) {
    			logger.error("Failed to Call User service response", e);
    			user.setSingleAttribute(IS_USER_PROFILE_CREATED, "false");
    			context.failure(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR); 
    		} 
        	catch (Exception e) {
    			logger.error("User Profile Authenticator failed", e);
    			user.setSingleAttribute(IS_USER_PROFILE_CREATED, "false");
    			context.failure(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR); 
    		} 
        } 
    }

    private String getUserServiceUrl(AuthenticationFlowContext context) {
        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        Map<String, String> config = configModel.getConfig();
        return config.get(UserProfileAuthenticatorFactory.USER_SERVICE_URL);
    }
    private String getUserServiceApiKey(AuthenticationFlowContext context) {
        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        Map<String, String> config = configModel.getConfig();
        return config.get(UserProfileAuthenticatorFactory.USER_SERVICE_URL);
    }
    private HttpClient buildHttpClient() {

		try {
			return (new HttpClientBuilder()) 
					.build();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create HTTP Client", e);
		}
	}
    @Override
    public void action(AuthenticationFlowContext context) {
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }

}
