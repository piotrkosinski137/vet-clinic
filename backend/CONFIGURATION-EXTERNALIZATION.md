# Configuration Externalization - Vet Clinic Backend

## Overview

This document outlines the configuration externalization improvements made to the Vet Clinic backend to support dev/staging/prod environments without code changes.

## Issues Found and Fixed

### 1. Hardcoded CORS Configuration
**Issue**: CORS origins were hardcoded as `http://localhost:3000` in `SecurityConfig.java`
```java
configuration.setAllowedOrigins(List.of("http://localhost:3000"));
configuration.setMaxAge(3600L);
```

**Fix**:
- Created new `CorsProperties` class with `@ConfigurationProperties`
- Added CORS configuration to `application.yml`
- Updated `SecurityConfig` to use injected `CorsProperties`
- Support comma-separated origins for multiple environments

### 2. @Value Annotations vs @ConfigurationProperties
**Issue**: `AuthController` used `@Value` annotations for Keycloak configuration
```java
@Value("${keycloak.auth-server-url:http://localhost:8180}")
@Value("${keycloak.realm:vetclinic}")
@Value("${keycloak.client-id:vetclinic-app}")
@Value("${keycloak.client-secret:vetclinic-secret}")
```

**Fix**:
- Created new `KeycloakProperties` class with `@ConfigurationProperties`
- Replaced individual `@Value` annotations with property injection
- Better validation and type safety
- Easier to extend with additional properties

### 3. Missing Configuration Properties
**Issue**: Keycloak realm, client-id, and client-secret were hardcoded or not externalized in all profiles

**Fix**:
- Updated `application.yml` to externalize all properties
- Added environment variable support for all properties
- Extended docker and postgres profiles with full Keycloak configuration

## Configuration Classes

### CorsProperties
**Location**: `application/src/main/java/com/vetclinic/config/CorsProperties.java`

Manages CORS configuration with support for:
- Multiple origins (comma-separated)
- HTTP methods
- Headers
- Credentials
- Max age (cache duration)

**Configuration prefix**: `cors`

**Properties**:
```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:3001
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600
```

### KeycloakProperties
**Location**: `application/src/main/java/com/vetclinic/config/KeycloakProperties.java`

Manages Keycloak OAuth2/OIDC configuration with support for:
- Auth server URL
- Realm name
- Client ID
- Client secret

**Configuration prefix**: `keycloak`

**Properties**:
```yaml
keycloak:
  auth-server-url: http://localhost:8180
  realm: vetclinic
  client-id: vetclinic-app
  client-secret: vetclinic-secret
```

## Environment Variables

### CORS Configuration
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000
CORS_MAX_AGE=3600
```

### Keycloak Configuration
```bash
KEYCLOAK_URL=http://localhost:8180
KEYCLOAK_REALM=vetclinic
KEYCLOAK_CLIENT_ID=vetclinic-app
KEYCLOAK_CLIENT_SECRET=vetclinic-secret
```

### Database Configuration
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vetclinic
SPRING_DATASOURCE_USERNAME=vetclinic
SPRING_DATASOURCE_PASSWORD=your_secure_password
SPRING_DATASOURCE_DRIVER=org.postgresql.Driver
```

### Server Configuration
```bash
SERVER_PORT=8080
```

## Profiles

### Default Profile (Docker PostgreSQL)
Used for local development with Docker PostgreSQL database.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5436/vetclinic
    username: vetclinic
    password: vetclinic
    driver-class-name: org.postgresql.Driver
```

**Usage**:
1. Start Docker PostgreSQL: `docker-compose up -d`
2. Run: `java -jar app.jar`

### Docker Profile
Used for Docker Compose environment with PostgreSQL and Keycloak services.

```bash
java -jar app.jar --spring.profiles.active=docker
```

**Configuration**:
- PostgreSQL: `postgres:5432/vetclinic`
- Keycloak: `http://keycloak:8180`

## Application Configuration Files

### application.yml
Main configuration file with defaults for development.

**Locations**:
- `application/src/main/resources/application.yml` - Main config
- `application/src/test/resources/application-test.yml` - Test config

## Migration Guide

### For Developers
1. Configuration is automatically loaded from `application.yml`
2. To override: use environment variables or command-line properties
3. CORS origins can be comma-separated: `CORS_ALLOWED_ORIGINS=http://a.com,http://b.com`

### For DevOps/Staging
Use environment variables instead of modifying configuration files:

```bash
docker run -e KEYCLOAK_URL=https://auth.staging.example.com \
           -e CORS_ALLOWED_ORIGINS=https://app.staging.example.com \
           -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/vetclinic \
           -e SPRING_DATASOURCE_PASSWORD=secure_password \
           --spring.profiles.active=docker \
           vet-clinic-app:latest
```

### For Production
All sensitive configuration should come from:
- Environment variables (for secrets)
- ConfigMaps/Secrets (for Kubernetes)
- Vault (for sensitive data)

```yaml
# DO NOT commit secrets to application.yml!
keycloak:
  client-secret: ${KEYCLOAK_CLIENT_SECRET}  # Use env variable

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:}  # Use env variable
```

## Security Considerations

1. **Client Secrets**: Always use environment variables for secrets in production
2. **CORS Origins**: Restrict to specific domains in production, never use wildcards
3. **Keycloak**: Uses environment-specific URLs
4. **Database**: Always use PostgreSQL with proper credentials

## Configuration Priority (Spring Boot)

1. Command-line arguments (`--property=value`)
2. Environment variables (`PROPERTY_NAME`)
3. `application-{profile}.yml` properties
4. `application.yml` properties
5. Hard-coded defaults in code

## Files Modified

### Created Files
- `application/src/main/java/com/vetclinic/config/CorsProperties.java`
- `application/src/main/java/com/vetclinic/config/KeycloakProperties.java`

### Modified Files
- `application/src/main/resources/application.yml` - Added CORS and Keycloak configuration
- `application/src/main/java/com/vetclinic/config/SecurityConfig.java` - Uses CorsProperties
- `application/src/main/java/com/vetclinic/config/AuthController.java` - Uses KeycloakProperties

## Validation

All configuration properties are now:
- Externalized (not hardcoded)
- Environment-aware (different values per profile)
- Type-safe (using @ConfigurationProperties)
- Well-documented (JavaDoc and comments)
- Support multiple values (comma-separated for origins)

## Testing

Configuration can be tested by:

```bash
# Test with environment variables
CORS_ALLOWED_ORIGINS=http://test.com \
KEYCLOAK_URL=http://test-keycloak:8180 \
mvn test

# Test with profiles
mvn test -Dspring.profiles.active=docker

# Test with system properties
mvn test -Dcors.allowed-origins=http://test.com
```

## Next Steps (Recommendations)

1. Add production-specific configuration for external secrets management (Vault/SecretsManager)
2. Add health checks that validate configuration on startup
3. Add audit logging for configuration changes
4. Document environment variable mappings in deployment documentation
