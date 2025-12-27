# Quick Configuration Reference

## What Was Changed

### 1. Created Configuration Classes
- **CorsProperties** - Manages CORS settings (origins, methods, headers, max-age)
- **KeycloakProperties** - Manages Keycloak OAuth2 settings

### 2. Updated Configuration Files
- **application.yml** - Added `cors` section and externalized all `keycloak` properties

### 3. Updated Java Classes
- **SecurityConfig** - Now uses injected `CorsProperties` instead of hardcoded values
- **AuthController** - Now uses injected `KeycloakProperties` instead of `@Value` annotations

## Environment Variables

### Keycloak
```bash
KEYCLOAK_URL=http://localhost:8180
KEYCLOAK_REALM=vetclinic
KEYCLOAK_CLIENT_ID=vetclinic-app
KEYCLOAK_CLIENT_SECRET=vetclinic-secret
```

### CORS
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000
CORS_MAX_AGE=3600
```

### Database
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vetclinic
SPRING_DATASOURCE_USERNAME=vetclinic
SPRING_DATASOURCE_PASSWORD=your_password
```

## Run with Different Configurations

### Development (Docker PostgreSQL)
```bash
# Start Docker containers first
docker-compose up -d

# Run the application
java -jar app.jar
```

### Docker Environment
```bash
java -jar app.jar --spring.profiles.active=docker
```

### Docker with Custom Environment Variables
```bash
docker run \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e KEYCLOAK_URL=https://auth.prod.com \
  -e CORS_ALLOWED_ORIGINS=https://app.prod.com \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  vet-clinic-app:latest
```

## Configuration Hierarchy (Spring Priority)

1. Command-line: `--keycloak.auth-server-url=value`
2. Environment: `KEYCLOAK_URL=value`
3. Profile YAML: `application-docker.yml`
4. Default YAML: `application.yml`
5. Code defaults: Class defaults

## Key Files

| File | Purpose | Status |
|------|---------|--------|
| `application.yml` | Main configuration | Modified - externalized |
| `SecurityConfig.java` | CORS setup | Modified - uses CorsProperties |
| `AuthController.java` | Keycloak token endpoint | Modified - uses KeycloakProperties |
| `CorsProperties.java` | CORS config class | Created |
| `KeycloakProperties.java` | Keycloak config class | Created |

## Multiple CORS Origins

To allow multiple origins, use comma-separated values:

```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,https://app.example.com
```

Or in YAML:
```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:3001,https://app.example.com
```

## Production Security Checklist

- [ ] Store `KEYCLOAK_CLIENT_SECRET` in secure vault
- [ ] Store `SPRING_DATASOURCE_PASSWORD` in secure vault
- [ ] Set `CORS_ALLOWED_ORIGINS` to specific domain(s), not wildcards
- [ ] Use HTTPS URLs for Keycloak
- [ ] Review all environment variables before deployment

## Documentation

- **CONFIGURATION-EXTERNALIZATION.md** - Full documentation with examples
- **CONFIGURATION-SUMMARY.txt** - Quick summary of changes
- **QUICK-CONFIG-REFERENCE.md** - This file
