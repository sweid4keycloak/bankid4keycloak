# BankID Identity Provider for Keycloak

BankID4Keycloak is an identity provider for Keycloak, giving it superpowers by adding support for Swedish BankID.
Please note, in order to use this in production a valid BankID contract is required, for more information see [this page](https://www.bankid.com/utvecklare/guider).


## Legal Notice

BankID is a registered trademark of Finansiell ID Teknik BID AB. We are not affiliated with, endorsed or sponsored by Finansiell ID Teknik BID AB.

Keycloak is a registered trademark of RedHat Inc. We are not affiliated with, endorsed or sponsored by RedHat Inc.


## Build and install

Clone the repository, enter the target directory and run

`mvn clean package`

Running the command above will create a jar-file in the *target* directory.

Copy the jar file to the Keycloak deployments directory. For further information on how to deploy components in Keycloak please see [this page](https://www.keycloak.org/docs/latest/server_development).

```
cp target/bankid4keycloak-1.0.0-SNAPSHOT.jar <KEYCLOAK_HOME>/providers
<KEYCLOAK_HOME>/bin/kc.[sh|bat] build
```

## Client certificate and truststore
In order to access the BankID API a client certificate and a truststore is required, both in PKCS12 format.


### Client certificate
To use BankID in production a valid contract is required, please contact one of the banks acting as resellers for [more information](https://www.bankid.com/en/utvecklare/guider/skapa-fp-certifikat).

A certificate for the BankID test environment can be downloaded using the following [page](https://www.bankid.com/en/utvecklare/test)

The password for the PKCS12 container and the private key is: qwerty123

### Truststore
The CA Certificate is available in the official [Integration guide, API](https://www.bankid.com/en/utvecklare/guider/teknisk-integrationsguide) under the [Environments](https://www.bankid.com/en/utvecklare/guider/teknisk-integrationsguide/miljoer) section. 


*example of how to create a PKCS12 truststore from a pem formated file*

`keytool -importcert -file Test_BankID_SSL_Root_CA_v1_Test.pem -alias "BankID Test CA" -trustcacerts -storetype pkcs12 -keystore truststore.p12`


## Configure

Start Keycloak and log in to the admin console.

Under the "Identity Providers" heading add a "BankID e-legitimation" identity provider.

### Client ID
This field is currently added as a required field to all identity providers by the core Keycloak theme. The BankID e-legitimation identity provider does not use this field so fill in any value. 

### Client Secret
This field is currently added as a required field to all identity providers by the core Keycloak theme. The BankID e-legitimation identity provider does not use this field so fill in any value. 

### Display order
Number defining the order of the providers in GUI (for example, on the Login page). The lowest number will be applied first.

### BankID API base URL
The URL for the BankID api. Please refer to the [BankID Relying Party Guidelines](https://www.bankid.com/bankid-i-dina-tjanster/rp-info) in case the URL has changed. +
At the time of writing they are 
 - Prod --> https://appapi2.bankid.com
 - Test --> https://appapi2.test.bankid.com

### Keystore file
Full path to the keystore file.

*example*: `/tls/keystore.p12`

### Keystore password

Password for the PKCS12 container.

*example*: `qwerty123`

### Password for the private key

The private key inside the PKCS12 container is also encrypted.

*example*: `qwerty123`

### Truststore file

Full path to the truststore file.

*example*: `/tls/truststore.p12`

### Truststore password

Password for the PKCS12 container.  

*example*: `qwerty123`

### Connection pool size

Upper bound for concurrent BankID calls. Leave default, only raise this if BankID calls are waiting on the pool.

### Connections per host

Maximum parallel connections to a single BankID endpoint. Keep this at or below the Connection pool size.
