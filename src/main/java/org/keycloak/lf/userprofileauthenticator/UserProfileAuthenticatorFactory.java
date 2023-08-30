package org.keycloak.lf.userprofileauthenticator;

import static java.util.Arrays.asList;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory; 
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class UserProfileAuthenticatorFactory  implements AuthenticatorFactory {
	 public static final String ID = "UserProfileAuthenticator";

	    private static final Authenticator AUTHENTICATOR_INSTANCE = new UserProfileAuthenticator();
	    static final String USER_SERVICE_URL = "user_service_url";
	    static final String USER_SERVICE_API_KEY = "user_service_api_key";

	    @Override
	    public Authenticator create(KeycloakSession keycloakSession) {
	        return AUTHENTICATOR_INSTANCE;
	    }

	    @Override
	    public String getDisplayType() {
	        return "User Profile Authenticator";
	    }

	    @Override
	    public boolean isConfigurable() {
	        return true;
	    }

	    @Override
	    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
	        return new AuthenticationExecutionModel.Requirement[] { AuthenticationExecutionModel.Requirement.REQUIRED };
	    }

	    @Override
	    public boolean isUserSetupAllowed() {
	        return false;
	    }

	    @Override
	    public String getHelpText() {
	        return "Call User Serivce to get the user dertails from SPAR";
	    }

	    @Override
	    public List<ProviderConfigProperty> getConfigProperties() {
	        ProviderConfigProperty userServiceSparCallEndPoint = new ProviderConfigProperty();

	        userServiceSparCallEndPoint.setType(STRING_TYPE);
	        userServiceSparCallEndPoint.setName(USER_SERVICE_URL);
	        userServiceSparCallEndPoint.setLabel("Url of LF User Service");
	        userServiceSparCallEndPoint.setHelpText("API endpoint that will pull the user information from SPAR");
	        
	        ProviderConfigProperty apiKey = new ProviderConfigProperty();

	        apiKey.setType(STRING_TYPE);
	        apiKey.setName(USER_SERVICE_API_KEY);
	        apiKey.setLabel("API Key of User Service");
	        apiKey.setHelpText("User Service API endpoint API Key, that will pull the user information from SPAR");
	        
	        return asList(userServiceSparCallEndPoint,apiKey);
	    }

	    @Override
	    public String getReferenceCategory() {
	        return null;
	    }

	    @Override
	    public void init(Config.Scope scope) {
	    }

	    @Override
	    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
	    }

	    @Override
	    public void close() {
	    }

	    @Override
	    public String getId() {
	        return ID;
	    }
}