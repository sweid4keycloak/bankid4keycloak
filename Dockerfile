FROM quay.io/keycloak/keycloak:26.0.4
COPY target/bankid4keycloak-*.jar /opt/keycloak/providers
