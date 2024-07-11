FROM quay.io/keycloak/keycloak:25.0.1
COPY target/bankid4keycloak-*.jar /opt/keycloak/providers
