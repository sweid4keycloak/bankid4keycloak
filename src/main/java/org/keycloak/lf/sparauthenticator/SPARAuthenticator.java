package org.keycloak.lf.sparauthenticator; 
import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*; 

public class SPARAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(SPARAuthenticator.class);
    private static final String SPAR_CALL_USER_ATTRIBUTE = "spar_call";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        //KeycloakSession session = context.getSession();
        //RealmModel realm = context.getRealm();
         UserModel user = context.getUser();
       
        String urlOfUserService = getUserServiceUrl(context);
        logger.error(user.getUsername());
        logger.error(user.getId());

        logger.error(urlOfUserService);
        // TODO
       /*   call user Service and 
	        If(user service return success){
	        //user.setSingleAttribute(SPAR_CALL_USER_ATTRIBUTE, "true"); 
	         * context.success();
	        }
	        else{
	        //context.cancelLogin(); validate if this call will also required
	        context.failure(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR);
	        } 
       */
         
        context.success();
    }

    private String getUserServiceUrl(AuthenticationFlowContext context) {
        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        Map<String, String> config = configModel.getConfig();
        return config.get(SparAuthenticatorFactory.USER_SERVICE_URL);
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
