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
ENV KC_METRICS_ENABLED=false

# JVM memory optimization for Railway 1GB limit
# Fixed heap sizes instead of percentages for predictable memory usage
ENV JAVA_OPTS_APPEND="-Xms256m -Xmx512m \
    -XX:MaxMetaspaceSize=128m \
    -XX:+UseSerialGC \
    -Xss512k \
    -Djava.security.egd=file:/dev/urandom"

# Copy startup script that converts Railway env vars to Keycloak format
COPY --chmod=755 start.sh /opt/keycloak/start.sh

ENTRYPOINT ["/opt/keycloak/start.sh"]
