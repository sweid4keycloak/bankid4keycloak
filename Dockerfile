FROM quay.io/keycloak/keycloak:20.0.5
COPY target/bankid4keycloak-*.jar /opt/keycloak/providers 
RUN /opt/keycloak/bin/kc.sh build
