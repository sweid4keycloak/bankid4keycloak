# BankID Identity Provider for Keycloak

BankID4Keycloak is a identity provider for keycloak, giving it superpowers by added support for the Swedish BankID.
To use this in produdction a valid subscibtion to BankID is required, for more information see --> #PAGE

We do however provde a guide on howto set this up against BankID test environment, so you should be up and running in a jiffy. 

## Build and install

Clone the reposotory, enter the target directory anf simply run

`mvn clean package`

Command abouve will a jar-file in the target directory.

Simple copy this jar file to the keycloak deployments directory. More on how to deploy components to keycloak please see.. --> PAGE

cp target/keycloak-bankid-idp-1.0.0-SNAPSHOT.jar <KEYCLOAK_HOME>/standalone/deployments/


## Client certificate and truststore
In order to access the BankID API a client certificate, is required. Also truststore, both in pkcs12 format.


### Client certificate
The client certificate for the produdction environment is obtained once you've got a valid subscribtion.

A certificate for BankID test environment however can be downloaded from the following page https://www.bankid.com/bankid-i-dina-tjanster/rp-info

The password for both pkcs12 container and the private key is: qwerty123

### Truststore
A truststore is also reuqired. The CA Certificate is avalibe in the PDF "BankID Relying Party Guidelines v3.2.22" also found on the page https://www.bankid.com/bankid-i-dina-tjanster/rp-info se page 13 for produdction environment and 14 for the test environment.

*example on howto create a pkcs12 truststore from a pem formated file*
`keytool -importcert -file apa.pem -alias "BankID Test CA" -trustcacerts -storetype pkcs12 -keystore truststore.p12`


## Configure

Start keycloak and log into the admin console.

Under "Identity Providers" add the "BankID e-legitimation" identity provider.

<picture>

**BankID API base URL:**
The URL for the BankID api. Please reffer to the "BankID Relying Party Guidelines v3.2.22" in case the URL has chnaged.
But at the time of writing they are
 - Prod --> https://appapi2.bankid.com/rp/v5
 - Test --> https://appapi2.test.bankid.com
 
**Keystore file:**
Full path to the keystore file
*example*
`/tls/keystore.p12`

**Keystore password:**
Password for the pkcs12 container.
*example*
`qwerty123`

**Password for the private key:**
The private key within the pkcs12 container is also encrypted.
*example*
`qwerty123`

**Truststore file:**
Full path to the truststore file
*example*
`/tls/truststore.p12`

**Truststore password:**
Password for the pkcs12 container.
*example*
`qwerty123`


## Usage

