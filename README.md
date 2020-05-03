# BankID Identity Provider for Keycloak

BankID4Keycloak is a identity provider for the keycloak server, giving it superpowers by added support for the Swedish BankID.
Please note, in order to use this in produdction a valid BankID subscibtion required, for more information see [this page](https://www.bankid.com/bankid-i-dina-tjanster/rp-info).

## Legal Notice

BankID is a registered trademark of Finansiell ID Teknik BID AB. We are not affiliated with, endorsed or sponsored by Finansiell ID Teknik BID AB.

Keycloak is a registered  trademark of RedHat Inc. We are not affiliated with, endorsed or sponsored by RedHat Inc.


## Build and install

Clone the reposotory, enter the target directory and simply run

`mvn clean package`

Command above will a jar-file in the directory *target*.

Simple copy this jar file to the keycloak deployments directory. More on howto deploy components to keycloak please see [https://www.keycloak.org/docs/latest/server_development](this page)

`cp target/bankid4keycloak-1.0.0-SNAPSHOT.jar <KEYCLOAK_HOME>/standalone/deployments/`


## Client certificate and truststore
In order to access the BankID API a client certificate and a truststore is required, both in pkcs12 format.


### Client certificate
The client certificate for the produdction environment is obtained once you've got a valid subscribtion.

A certificate for BankID test environment however can be downloaded from the following [page](https://www.bankid.com/bankid-i-dina-tjanster/rp-info)

The password for both pkcs12 container and the private key is: qwerty123

### Truststore
The CA Certificate is available in the PDF "BankID Relying Party Guidelines v3.2.22" also found on the [page](https://www.bankid.com/bankid-i-dina-tjanster/rp-info) see page 13 for produdction environment and 14 for the test environment.

*example on howto create a pkcs12 truststore from a pem formated file*
`keytool -importcert -file apa.pem -alias "BankID Test CA" -trustcacerts -storetype pkcs12 -keystore truststore.p12`


## Configure

Start keycloak and log into the admin console.

Under "Identity Providers" add the "BankID e-legitimation" identity provider.

<picture>

**BankID API base URL:**
The URL for the BankID api. Please reffer to the "" in case the URL has changed.
But at the time of writing they are
 - Prod --> https://appapi2.bankid.com
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
The private key within the pkcs12 container is also encrypted. [BankID Relying Party Guidelines v3.2.22](https://www.bankid.com/bankid-i-dina-tjanster/rp-info)
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
