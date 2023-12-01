# BankID Identity Provider for Keycloak

BankID4Keycloak is an identity provider for Keycloak, giving it superpowers by adding support for Swedish BankID.
Please note, in order to use this in production a valid BankID contract is required, for more information see [this page](https://www.bankid.com/utvecklare/guider).

:warning: Since `Keycloak v21.0.0` this extension cannot be used effectively. The old AngularJS-based Admin-UI got removed completely and at least for now there's no way of displaying all necessary configuration options in order to configure this extension properly in Keycloak. To fix this there needs to be changes made to code Keycloak see [keycloak #15344](https://github.com/keycloak/keycloak/issues/15344).
For now, the latest version this extension is compatible with is `v20.0.5` provided that you switch the admin theme to `keycloak`.

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
To use BankID in production a valid contract is required, please contact one of the banks acting as resellers for [more information](https://www.bankid.com/utvecklare/guider/skapa-fp-certifikat).

A certificate for the BankID test environment can be downloaded using the following [page](https://www.bankid.com/utvecklare/test)

The password for the PKCS12 container and the private key is: qwerty123

### Truststore
The CA Certificate is available in the PDF "BankID Relying Party Guidelines v3.2.22" and can also found on this [page](https://www.bankid.com/bankid-i-dina-tjanster/rp-info). See pages 13 and 14 of the PDF for production and test certificates.

*example of how to create a PKCS12 truststore from a pem formated file*  
`keytool -importcert -file apa.pem -alias "BankID Test CA" -trustcacerts -storetype pkcs12 -keystore truststore.p12`


## Configure

Start Keycloak and log in to the admin console.

:warning: It's not yet compatible to the new `Admin UI (keycloak.v2)` from Keycloak. If you want to use this provider, you need to enable
the old Admin UI for the respective realm (mostly `master`, see this 
paper [Keycloak 19.0.0 release](https://www.keycloak.org/2022/07/keycloak-1900-released.html#_new_admin_console_is_now_the_default_console))


Under the "Identity Providers" heading add the "BankID e-legitimation" identity provider.

**BankID API base URL:**
The URL for the BankID api. Please refer to the [BankID Relying Party Guidelines](https://www.bankid.com/bankid-i-dina-tjanster/rp-info) in case the URL has changed. 
At the time of writing they are 
 - Prod --> https://appapi2.bankid.com
 - Test --> https://appapi2.test.bankid.com

**Keystore file:**  
Full path to the keystore file.  
*example*  
`/tls/keystore.p12`

**Keystore password:**  
Password for the PKCS12 container.  
*example*  
`qwerty123`

**Password for the private key:**  
The private key inside the PKCS12 container is also encrypted.  
*example*  
`qwerty123`

**Truststore file:**  
Full path to the truststore file.  
*example*  
`/tls/truststore.p12`

**Truststore password:**  
Password for the PKCS12 container.  
*example*  
`qwerty123`

## Auth flows
The plugin supports login flows based on API or WebView. Clients decide which flow to use via the querystring `bankid_login_flow` in the auth step.
Valid values are `api` and `webview`. Defaults to `webview`.

### API flow

1. Initiate authentication using `<KEYCLOAK_URL>/realms/<REALM>/protocol/openid-connect/auth`.
2. Start polling the `pollingUrl` and wait for the BankID identification to complete.
3. [Launch](https://www.bankid.com/en/utvecklare/guider/teknisk-integrationsguide/programstart) the BankID application using the autostarttoken.
4. When identification is done, the collect endpoint will return status `complete`.
5. Open completion url to finalize the authentication, follow the redirect and grab the code.
6. Exchange the code for a token at `<KEYCLOAK_URL>/realms/<REALM>/protocol/openid-connect/token`

**Auth start:**
(Internally redirected from Keycloak auth endpoint when using API login flow)
```
GET /api/start
{
    "pollingUrl": "<POLLING_URL>", // /api/collect
    "cancelUrl": "<CANCEL_URL>", // /api/cancel
    "autostarttoken": "<AUTOSTART_TOKEN>"
}
```

**Collect:**
```
GET /api/collect
{
    "status": "<BANKID_STATUS>", // pending | complete | failed
    "hintCode": "<BANKID_HINT_CODE", // Only present for failed orders, defaults to null
    "messageShortName": "<BANKID_MESSAGE_SHORT_NAME>", // Only present for failed orders, defaults to null
    "completionUrl": "<COMPLETION_URL>" // /api/done
}
```

**Done:**
```
GET /api/done
Redirects back to client application with authorization code
```

**Cancel:**
```
GET /api/cancel
{
    "status": "cancelled",
    "hintCode": "<BANKID_HINT_CODE>",
    "messageShortName": "<BANKID_MESSAGE_SHORT_NAME>"
}
```