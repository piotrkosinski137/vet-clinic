# Railway Deployment Setup

## Services Required
- **Postgres** - Single shared database
- **keycloak** - Authentication server
- **backend** - Spring Boot API
- **frontend** - React app

## One-Time Variable Setup

### For `keycloak` service, add these variable references:
```
PGHOST = ${{Postgres.PGHOST}}
PGPORT = ${{Postgres.PGPORT}}
PGUSER = ${{Postgres.PGUSER}}
PGPASSWORD = ${{Postgres.PGPASSWORD}}
PGDATABASE = ${{Postgres.PGDATABASE}}
```

### For `backend` service, add these variable references:
```
PGHOST = ${{Postgres.PGHOST}}
PGPORT = ${{Postgres.PGPORT}}
PGUSER = ${{Postgres.PGUSER}}
PGPASSWORD = ${{Postgres.PGPASSWORD}}
PGDATABASE = ${{Postgres.PGDATABASE}}
```

### For `backend` service, also add Keycloak config:
```
KEYCLOAK_ISSUER_URI = https://<keycloak-domain>/realms/vetclinic
KEYCLOAK_JWK_URI = https://<keycloak-domain>/realms/vetclinic/protocol/openid-connect/certs
KEYCLOAK_URL = https://<keycloak-domain>
```

### For `frontend` service:
Environment variables are baked in at build time via `.env.production`

## Root Directories (set in Railway dashboard)
- keycloak: `keycloak`
- backend: `backend`
- frontend: `frontend`

## Auto-Deploy
Push to `develop` branch triggers automatic deployment of all services.
