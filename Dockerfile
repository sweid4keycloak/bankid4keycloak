FROM jboss/keycloak:15.0.1
ADD target/bankid4keycloak-1.0.0-SNAPSHOT.jar /opt/jboss/keycloak/standalone/deployments/
