#!/bin/bash
# Convert Railway's Postgres env vars to Keycloak's format

export KC_DB_URL_HOST="${PGHOST:-localhost}"
export KC_DB_URL_PORT="${PGPORT:-5432}"
export KC_DB_URL_DATABASE="${PGDATABASE:-railway}"
export KC_DB_USERNAME="${PGUSER:-postgres}"
export KC_DB_PASSWORD="${PGPASSWORD:-postgres}"

exec /opt/keycloak/bin/kc.sh start --import-realm --optimized
