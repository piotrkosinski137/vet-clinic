# Railway Deployment Setup & Troubleshooting Guide

This document contains all issues encountered during Railway deployment and their solutions. Use this as a QA source of truth for future deployments.

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Initial Setup](#initial-setup)
- [Common Issues & Solutions](#common-issues--solutions)
  - [Frontend Issues](#frontend-issues)
  - [Backend Issues](#backend-issues)
  - [Keycloak Issues](#keycloak-issues)
  - [Database Issues](#database-issues)
- [Variable Reference Syntax](#variable-reference-syntax)
- [Final Configuration](#final-configuration)

---

## Architecture Overview

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    Frontend     │────▶│     Backend     │────▶│    Postgres     │
│  (React/Vite)   │     │  (Spring Boot)  │     │   (Shared DB)   │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
        │                       │                        │
        │                       ▼                        │
        │               ┌─────────────────┐              │
        └──────────────▶│    Keycloak     │◀─────────────┘
                        │     (Auth)      │
                        └─────────────────┘
```

**Services:**
- **Postgres** - Single shared database for backend and keycloak
- **Keycloak** - Authentication server (realm: vetclinic)
- **Backend** - Spring Boot API
- **Frontend** - React/Vite app served by nginx

---

## Initial Setup

### Root Directories (Railway Dashboard → Service → Settings)
| Service | Root Directory |
|---------|----------------|
| keycloak | `keycloak` |
| backend | `backend` |
| frontend | `frontend` |

### Auto-Deploy
- Push to `develop` branch triggers automatic deployment
- Each service rebuilds only when its root directory changes

---

## Common Issues & Solutions

### Frontend Issues

#### Issue: nginx "invalid port ${PORT:-80}"
**Symptom:**
```
nginx: [emerg] invalid port in "${PORT:-80}" of the "listen" directive
```

**Cause:** nginx config uses `${PORT:-80}` but envsubst wasn't running to substitute the variable.

**Solution:**
1. Use plain `$PORT` in nginx.conf (not `${PORT:-80}`)
2. Create startup script that runs envsubst before nginx:
```dockerfile
# In Dockerfile
COPY docker/nginx.conf /etc/nginx/nginx.conf.template
RUN echo '#!/bin/sh' > /start.sh && \
    echo 'envsubst "\$PORT" < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf' >> /start.sh && \
    echo 'exec nginx -g "daemon off;"' >> /start.sh && \
    chmod +x /start.sh
CMD ["/start.sh"]
```

---

#### Issue: API calls going to frontend URL instead of backend
**Symptom:**
```
Request URL: https://frontend-production-xxx.up.railway.app/api/v1/clients
(Should be: https://backend-production-xxx.up.railway.app/api/v1/clients)
```

**Cause:** Hardcoded `/api/v1` in `frontend/src/api/client.ts` instead of using environment config.

**Solution:**
```typescript
// Before (wrong)
const API_BASE = '/api/v1';

// After (correct)
import { config } from '../config/env';
const API_BASE = config.api.baseUrl;
```

The `config.api.baseUrl` reads from `VITE_API_BASE_URL` environment variable, which is set in `.env.production`.

---

### Backend Issues

#### Issue: mvnw permission denied
**Symptom:**
```
/bin/sh: ./mvnw: Permission denied
```

**Cause:** Maven wrapper file doesn't have execute permission in Docker.

**Solution:** Add chmod in Dockerfile:
```dockerfile
COPY mvnw pom.xml ./
RUN chmod +x mvnw
```

---

#### Issue: Child module does not exist
**Symptom:**
```
[ERROR] Child module /app/veterinarian-module of /app/pom.xml does not exist
[ERROR] Child module /app/visit-module of /app/pom.xml does not exist
```

**Cause:** Dockerfile only copies some modules but pom.xml references all modules.

**Solution:** Copy ALL modules in Dockerfile:
```dockerfile
COPY common/pom.xml common/
COPY patient-module/pom.xml patient-module/
COPY client-module/pom.xml client-module/
COPY veterinarian-module/pom.xml veterinarian-module/
COPY visit-module/pom.xml visit-module/
COPY billing-module/pom.xml billing-module/
COPY compliance-module/pom.xml compliance-module/
COPY application/pom.xml application/
```

---

#### Issue: Logstash connection refused warnings (spam in logs)
**Symptom:**
```
WARN LogstashTcpSocketAppender[LOGSTASH] - Log destination localhost:5000: connection failed
```

**Cause:** Logstash appender defined globally in logback-spring.xml, gets initialized even for railway profile.

**Solution:** Wrap Logstash appender inside springProfile:
```xml
<!-- Only for docker/logstash profiles -->
<springProfile name="docker,logstash">
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        ...
    </appender>
</springProfile>

<!-- Railway profile - console only, no Logstash -->
<springProfile name="railway">
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</springProfile>
```

---

#### Issue: CORS error - blocked by CORS policy
**Symptom:**
```
Access to fetch blocked by CORS policy: No 'Access-Control-Allow-Origin' header
```

**Cause:** Backend CORS config doesn't include frontend Railway URL.

**Solution:** Set default CORS origin in `application-railway.yml`:
```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:https://frontend-production-6b31.up.railway.app}
```

---

#### Issue: Missing SPRING_PROFILES_ACTIVE
**Symptom:** Backend uses wrong profile, wrong database config.

**Solution:** Set in Dockerfile:
```dockerfile
ENV SPRING_PROFILES_ACTIVE=railway
```

---

### Keycloak Issues

#### Issue: Out of Memory (OOM) crash
**Symptom:** Keycloak container keeps restarting, logs show memory errors.

**Cause:** Keycloak is memory-hungry, Railway free tier has limited memory.

**Solution:** Add JVM memory limits in Dockerfile:
```dockerfile
ENV JAVA_OPTS_APPEND="-XX:MaxRAMPercentage=70.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/urandom"
```

---

#### Issue: Multiple garbage collectors selected
**Symptom:**
```
Error occurred during initialization of VM
Multiple garbage collectors selected
```

**Cause:** Added `-XX:+UseG1GC` but Keycloak image already has a GC configured.

**Solution:** Remove G1GC option, only keep memory limits:
```dockerfile
# Wrong
ENV JAVA_OPTS_APPEND="-XX:MaxRAMPercentage=70.0 -XX:+UseG1GC ..."

# Correct
ENV JAVA_OPTS_APPEND="-XX:MaxRAMPercentage=70.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/urandom"
```

---

#### Issue: chmod permission denied in Keycloak image
**Symptom:**
```
chmod: changing permissions of '/opt/keycloak/start.sh': Operation not permitted
```

**Cause:** Keycloak image runs as non-root user, can't chmod files.

**Solution:** Use `--chmod` flag in COPY:
```dockerfile
# Wrong
COPY start.sh /opt/keycloak/start.sh
RUN chmod +x /opt/keycloak/start.sh

# Correct
COPY --chmod=755 start.sh /opt/keycloak/start.sh
```

---

#### Issue: Failed to obtain JDBC connection
**Symptom:**
```
ERROR: Failed to obtain JDBC connection
ERROR: Acquisition timeout while waiting for new connection
```

**Cause:** Keycloak doesn't have Postgres connection variables (PGHOST, PGPORT, etc.)

**Solution:**
1. In Railway dashboard, go to keycloak → Variables
2. Add variable references to shared Postgres (see Variable Reference Syntax below)

---

#### Issue: 502 Bad Gateway on Keycloak endpoints
**Symptom:** Keycloak appears running but returns 502 on all requests.

**Cause:** Keycloak not listening on Railway's PORT environment variable.

**Solution:** Set KC_HTTP_PORT from PORT in start.sh:
```bash
#!/bin/bash
export KC_HTTP_PORT="${PORT:-8080}"
exec /opt/keycloak/bin/kc.sh start --import-realm --optimized
```

---

#### Issue: Invalid user credentials after database change
**Symptom:**
```
{error: "invalid_grant", error_description: "Invalid user credentials"}
```

**Cause:** After switching to shared Postgres, the database is empty - realm wasn't imported.

**Solution:** Redeploy keycloak service to trigger realm import on fresh database.

---

### Database Issues

#### Issue: Multiple Postgres instances wasting resources
**Problem:** Initially created 3 separate Postgres databases (one each for backend, keycloak, and a spare).

**Solution:** Consolidate to 1 shared Postgres:
1. Delete extra Postgres services
2. Keep one Postgres
3. Both backend and keycloak use the same Postgres instance
4. Tables don't conflict (different schemas)

---

#### Issue: Keycloak tables not appearing in Postgres
**Cause:** Keycloak can't connect to database or hasn't started properly.

**Check:** After successful Keycloak startup, you should see ~90 tables (realm, user_entity, client, etc.) in Postgres → Data tab.

---

## Variable Reference Syntax

Railway uses `${{ServiceName.VARIABLE}}` syntax for referencing variables from other services.

### For `keycloak` service variables:
```
PGHOST      = ${{Postgres.PGHOST}}
PGPORT      = ${{Postgres.PGPORT}}
PGUSER      = ${{Postgres.PGUSER}}
PGPASSWORD  = ${{Postgres.PGPASSWORD}}
PGDATABASE  = ${{Postgres.PGDATABASE}}
```

### For `backend` service variables:
```
PGHOST      = ${{Postgres.PGHOST}}
PGPORT      = ${{Postgres.PGPORT}}
PGUSER      = ${{Postgres.PGUSER}}
PGPASSWORD  = ${{Postgres.PGPASSWORD}}
PGDATABASE  = ${{Postgres.PGDATABASE}}
```

### Additional backend variables:
```
KEYCLOAK_ISSUER_URI = https://<keycloak-domain>/realms/vetclinic
KEYCLOAK_JWK_URI    = https://<keycloak-domain>/realms/vetclinic/protocol/openid-connect/certs
KEYCLOAK_URL        = https://<keycloak-domain>
```

**Note:** Replace `<keycloak-domain>` with actual domain, e.g., `keycloak-production-0590.up.railway.app`

---

## Final Configuration

### URLs (example)
| Service | URL |
|---------|-----|
| Frontend | `https://frontend-production-6b31.up.railway.app` |
| Backend | `https://backend-production-0857.up.railway.app` |
| Keycloak | `https://keycloak-production-0590.up.railway.app` |

### Default Credentials
- **Username:** `admin`
- **Password:** `admin123!`

### Files Modified for Railway Support
| File | Changes |
|------|---------|
| `frontend/Dockerfile` | nginx with envsubst for PORT |
| `frontend/docker/nginx.conf` | Uses $PORT variable |
| `frontend/src/api/client.ts` | Uses config for API base URL |
| `frontend/.env.production` | Railway URLs for Keycloak and backend |
| `backend/Dockerfile` | chmod mvnw, all modules, SPRING_PROFILES_ACTIVE |
| `backend/application/src/main/resources/application-railway.yml` | Railway-specific config |
| `backend/application/src/main/resources/logback-spring.xml` | Railway profile without Logstash |
| `keycloak/Dockerfile` | Memory limits, start.sh with chmod |
| `keycloak/start.sh` | Converts PG* to KC_DB_* vars, sets PORT |
| `keycloak/realm-export.json` | Production realm with admin user |

### Local Development
Local docker-compose uses single Postgres with init script to create both databases:
- `docker/postgres/init-databases.sql` - Creates `keycloak` database
- Backend uses `vetclinic` database (default)
- Keycloak uses `keycloak` database

---

## Quick Troubleshooting Checklist

1. **Service won't start?**
   - Check Railway logs (Deployments → click deployment → Logs)
   - Verify root directory is set correctly

2. **Database connection failed?**
   - Check PG* variable references are set
   - Verify Postgres service is running
   - Check variable syntax: `${{Postgres.PGHOST}}` not `${{POSTGRES_DB.PGHOST}}`

3. **502 Bad Gateway?**
   - Service not listening on correct PORT
   - Check if PORT env var is being used

4. **CORS errors?**
   - Verify CORS_ALLOWED_ORIGINS includes frontend URL
   - Check application-railway.yml has correct default

5. **Auth not working?**
   - Check Keycloak is running
   - Verify realm was imported (check for tables in Postgres)
   - Redeploy keycloak if realm is missing

6. **Changes not deploying?**
   - Push was to wrong branch (must be `develop`)
   - Root directory doesn't match changed files
   - Manually trigger redeploy from Railway dashboard
