FROM docker.io/library/maven:3-eclipse-temurin-11 as builder
WORKDIR /app
COPY . .
RUN mvn clean package

FROM quay.io/keycloak/keycloak:20.0.5
COPY --from=builder /app/target/bankid4keycloak-*.jar /opt/keycloak/providers
