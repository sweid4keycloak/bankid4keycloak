# BankID Identity Provider for Keycloak

*NOTE*: This currently does NOT authenticate towards BankID instead a call to the Deck of Cards API will be 
made to create a user with a username, firstname and lastname based on a drawn card.

## Build and install

mvn clean package

cp target/keycloak-bankid-idp-1.0.0-SNAPSHOT.jar <KEYCLOAK_HOME>/standalone/deployments/

## Configure

Start keycloak and go into the admin console

Under "Identity Providers" add the "BankID e-legitimation" identity provider.

