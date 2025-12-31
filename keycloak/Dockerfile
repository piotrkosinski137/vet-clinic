FROM quay.io/keycloak/keycloak:24.0 AS builder

ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

WORKDIR /opt/keycloak
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:24.0

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY realm-export.json /opt/keycloak/data/import/

ENV KC_DB=postgres
ENV KC_HTTP_ENABLED=true
ENV KC_HOSTNAME_STRICT=false
ENV KC_PROXY=edge
ENV KC_HEALTH_ENABLED=true

# Limit JVM memory for Railway free tier
ENV JAVA_OPTS_APPEND="-XX:MaxRAMPercentage=70.0 -XX:InitialRAMPercentage=50.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Djava.security.egd=file:/dev/urandom"

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start", "--import-realm", "--optimized"]
