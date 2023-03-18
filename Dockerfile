FROM quay.io/keycloak/keycloak:20.0.5
COPY target/bankid4keycloak-*.jar /opt/keycloak/providers
