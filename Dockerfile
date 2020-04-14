FROM jboss/keycloak:9.0.2
ADD target/keycloak-bankid-idp-1.0.0-SNAPSHOT.jar /opt/jboss/keycloak/standalone/deployments/
