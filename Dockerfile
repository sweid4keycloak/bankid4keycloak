FROM quay.io/keycloak/keycloak:24.0.3
COPY target/bankid4keycloak-*.jar /opt/keycloak/providers
