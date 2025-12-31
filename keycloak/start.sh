#!/bin/bash
# Convert Railway's env vars to Keycloak's format

# Database connection
export KC_DB_URL_HOST="${PGHOST:-localhost}"
export KC_DB_URL_PORT="${PGPORT:-5432}"
export KC_DB_URL_DATABASE="${PGDATABASE:-railway}"
export KC_DB_USERNAME="${PGUSER:-postgres}"
export KC_DB_PASSWORD="${PGPASSWORD:-postgres}"

# Database pool optimization for low memory
export KC_DB_POOL_MIN_SIZE=1
export KC_DB_POOL_INITIAL_SIZE=1
export KC_DB_POOL_MAX_SIZE=5

# HTTP port - Railway provides PORT env var
export KC_HTTP_PORT="${PORT:-8080}"

exec /opt/keycloak/bin/kc.sh start --import-realm --optimized
