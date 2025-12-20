# Project Structure

## Overview

This is a **Veterinary Clinic Management System** built with modern Spring Boot 3.3.x architecture.

## Technology Stack

### Backend
- **Java**: 21 (LTS)
- **Spring Boot**: 3.3.0
- **Spring Security**: OAuth 2.0 Resource Server with JWT
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA with Hibernate
- **Migrations**: Flyway
- **Authentication**: Keycloak 24.0
- **API Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Maven 3.9.2

### Code Quality
- **Formatting**: Spotless with Google Java Format (AOSP style)
- **Static Analysis**: Checkstyle, SpotBugs
- **Code Coverage**: JaCoCo
- **Architecture Testing**: ArchUnit

### Testing
- **Framework**: JUnit 5, Spring Boot Test
- **Integration Tests**: Testcontainers (PostgreSQL)
- **Mocking**: Mockito

### Infrastructure
- **Containers**: Docker & Docker Compose
- **CI/CD**: GitHub Actions (configured)

## Directory Structure

```
klinikaxp/
├── .claude/                          # Claude Code documentation
│   ├── DEVELOPMENT.md               # Main development guide
│   ├── QUICK_REFERENCE.md           # Quick command reference
│   ├── WINDOWS_SPECIFICS.md         # Windows-specific setup
│   ├── PROJECT_STRUCTURE.md         # This file
│   └── settings.local.json          # Claude Code permissions
│
├── backend/                          # Spring Boot application
│   ├── common/                       # Shared utilities
│   │   ├── src/main/java/com/vetclinic/common/
│   │   │   ├── dto/                 # Common DTOs
│   │   │   ├── exception/           # Custom exceptions
│   │   │   └── validation/          # Validation utilities
│   │   └── pom.xml
│   │
│   ├── patient-module/               # Patient domain module
│   │   ├── src/main/java/com/vetclinic/patient/
│   │   │   ├── model/               # Patient entities
│   │   │   ├── repository/          # JPA repositories
│   │   │   ├── service/             # Business logic
│   │   │   ├── dto/                 # Patient DTOs
│   │   │   └── mapper/              # MapStruct mappers
│   │   ├── src/main/resources/
│   │   │   └── db/migration/        # Flyway migrations
│   │   └── pom.xml
│   │
│   ├── client-module/                # Client domain module
│   │   ├── src/main/java/com/vetclinic/client/
│   │   │   ├── model/               # Client entities
│   │   │   ├── repository/          # JPA repositories
│   │   │   ├── service/             # Business logic
│   │   │   ├── dto/                 # Client DTOs
│   │   │   └── mapper/              # MapStruct mappers
│   │   ├── src/main/resources/
│   │   │   └── db/migration/        # Flyway migrations
│   │   └── pom.xml
│   │
│   ├── application/                  # Main application module
│   │   ├── src/main/java/com/vetclinic/
│   │   │   ├── VetClinicApplication.java  # Main class
│   │   │   ├── config/              # Configuration classes
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── FlywayConfig.java
│   │   │   ├── controller/          # REST controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── PatientController.java
│   │   │   │   └── ClientController.java
│   │   │   └── exception/           # Global exception handling
│   │   │       └── GlobalExceptionHandler.java
│   │   ├── src/main/resources/
│   │   │   ├── application.yml      # Main config
│   │   │   ├── application-docker.yml  # Docker profile
│   │   │   └── db/migration/        # Application-level migrations
│   │   ├── src/test/java/
│   │   │   ├── com/vetclinic/
│   │   │   │   ├── integration/     # Integration tests
│   │   │   │   └── architecture/    # ArchUnit tests
│   │   └── pom.xml
│   │
│   └── pom.xml                       # Parent POM
│
├── docker/                           # Docker configuration
│   └── keycloak/                     # Keycloak realm import
│       └── vetclinic-realm.json     # Realm configuration
│
├── .github/                          # GitHub Actions workflows
│   └── workflows/
│       ├── ci.yml                   # Main CI pipeline
│       └── pr-validation.yml        # PR validation
│
├── docker-compose.yml                # Local development services
├── checkstyle.xml                    # Checkstyle configuration
├── spotbugs-exclude.xml              # SpotBugs exclusions
├── .gitignore                        # Git ignore rules
└── README.md                         # Project documentation
```

## Module Dependency Graph

```
application
    ├── patient-module
    │   └── common
    ├── client-module
    │   └── common
    └── common
```

## Architecture

### Modular Monolith

The application follows a modular monolith architecture:

1. **Common Module**: Shared utilities, exceptions, base DTOs
2. **Domain Modules**: Patient, Client (each with own entities, repositories, services)
3. **Application Module**: REST API, security, configuration

### Layered Architecture

Each domain module follows a layered architecture:

```
Controller Layer (in application module)
    ↓
Service Layer (business logic)
    ↓
Repository Layer (data access)
    ↓
Database (PostgreSQL)
```

### Key Patterns

- **DTO Pattern**: Separate DTOs for API requests/responses
- **Mapper Pattern**: MapStruct for entity-DTO conversion
- **Repository Pattern**: Spring Data JPA repositories
- **Exception Handling**: Centralized with @RestControllerAdvice
- **Configuration**: Environment-specific profiles (default, docker)

## Database Schema

### Patient Module

**Table**: `patients`
- `id` (UUID, PK)
- `name` (VARCHAR)
- `species` (VARCHAR)
- `breed` (VARCHAR)
- `age` (INTEGER)
- `owner_id` (UUID, FK to clients)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Client Module

**Table**: `clients`
- `id` (UUID, PK)
- `first_name` (VARCHAR)
- `last_name` (VARCHAR)
- `email` (VARCHAR)
- `phone` (VARCHAR)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

## API Endpoints

### Authentication
- `POST /auth/token` - Get JWT token
- `POST /auth/refresh` - Refresh token

### Patients
- `GET /api/v1/patients` - List all patients
- `GET /api/v1/patients/{id}` - Get patient by ID
- `POST /api/v1/patients` - Create patient
- `PUT /api/v1/patients/{id}` - Update patient
- `DELETE /api/v1/patients/{id}` - Delete patient

### Clients
- `GET /api/v1/clients` - List all clients
- `GET /api/v1/clients/{id}` - Get client by ID
- `POST /api/v1/clients` - Create client
- `PUT /api/v1/clients/{id}` - Update client
- `DELETE /api/v1/clients/{id}` - Delete client

### Documentation
- `GET /swagger-ui.html` - Swagger UI
- `GET /v3/api-docs` - OpenAPI JSON

## Security

### Authentication Flow

1. User sends credentials to `/auth/token`
2. Backend validates credentials with Keycloak
3. Keycloak returns JWT token
4. User includes token in `Authorization: Bearer <token>` header
5. Spring Security validates token with Keycloak's public keys

### Keycloak Configuration

**Realm**: vetclinic
**Client**: vetclinic-app
**Roles**: user, admin

**Default Users**:
- Username: `user`, Password: `user`, Role: `user`

## Configuration Profiles

### Default Profile
- Uses local PostgreSQL (localhost:5432)
- Uses local Keycloak (localhost:8180)
- Development settings

### Docker Profile
- Uses Docker service names for connection
- PostgreSQL: `postgres:5432`
- Keycloak: `keycloak:8180`
- Production-ready settings

## Build Lifecycle

### Maven Build Phases

1. **validate**: Checkstyle, Spotless check
2. **compile**: Java compilation with MapStruct
3. **test**: Unit tests with JaCoCo
4. **verify**: SpotBugs, integration tests
5. **package**: JAR creation
6. **install**: Install to local Maven repo

### Quality Gates

**Enforced by -Pci profile**:
- Checkstyle: No style violations
- Spotless: Code must be formatted
- SpotBugs: No high/medium bugs
- JaCoCo: Minimum coverage (currently 0%, can be adjusted)

## Development Workflow

### Standard Development Cycle

1. **Start Services**: `docker-compose up -d`
2. **Develop**: Make code changes
3. **Format**: `mvn spotless:apply -q`
4. **Test**: `mvn test -Pfast`
5. **Run**: `mvn spring-boot:run -pl application -Pfast`
6. **Verify**: `curl http://localhost:8080/actuator/health`

### Pre-Commit Checklist

1. Format code: `mvn spotless:apply -q`
2. Run tests: `mvn verify -Pfast`
3. Check build: `mvn clean verify -Pci`
4. Commit changes

## Testing Strategy

### Unit Tests
- Test individual classes in isolation
- Mock dependencies with Mockito
- Fast execution, no external dependencies

### Integration Tests
- Test full request-response cycle
- Use Testcontainers for PostgreSQL
- Test database interactions
- Verify API contracts

### Architecture Tests
- ArchUnit rules to enforce:
  - Layering constraints
  - Naming conventions
  - Dependency rules
  - Package structure

## Docker Services

### PostgreSQL (postgres:16-alpine)
- **Port**: 5432
- **Database**: vetclinic
- **User/Password**: vetclinic/vetclinic
- **Volume**: postgres_data (persistent)

### Keycloak (quay.io/keycloak/keycloak:24.0)
- **Port**: 8180
- **Admin**: admin/admin
- **Realm**: vetclinic (auto-imported)
- **Volume**: keycloak_data (persistent)

### Backend (optional, --profile full)
- **Port**: 8080
- **Profile**: docker
- **Depends on**: postgres, keycloak

## Monitoring & Observability

### Spring Boot Actuator

Endpoints (exposed):
- `/actuator/health` - Health check
- `/actuator/info` - Application info

Additional endpoints (production):
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

## Future Enhancements

Potential additions:
- Appointments module
- Medical records module
- Billing module
- Notifications (email/SMS)
- File storage for medical images
- Reporting and analytics
- Frontend (React/Angular/Vue)
- Mobile app
- Multi-tenancy support

## Known Limitations

1. **Authentication**: Currently uses Keycloak direct token exchange, should use proper OAuth 2.0 flow in production
2. **Error Handling**: Basic global exception handler, needs more specific error codes
3. **Validation**: Basic validation, needs more comprehensive business rules
4. **Logging**: Using default logging, should add structured logging (e.g., with ELK stack)
5. **Monitoring**: Basic Actuator endpoints, needs APM (Application Performance Monitoring)
6. **Caching**: No caching implemented, could add Redis for performance
7. **Rate Limiting**: No rate limiting on API endpoints
8. **File Upload**: Not implemented yet (for medical records, images, etc.)
