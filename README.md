# Vet Clinic Application

A modular monolithic veterinary clinic management system built with Java 21, Spring Boot 3.3, and React.

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Docker & Docker Compose
- Node.js 20+ (for frontend - coming soon)

## Quick Start

### Option 1: Run with H2 (In-Memory Database)

No setup required - just run:

```bash
cd backend
./mvnw spring-boot:run -pl application
```

Access:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### Option 2: Run with PostgreSQL (Docker)

1. Start PostgreSQL:
```bash
docker-compose up -d
```

2. Run the application with postgres profile:
```bash
cd backend
./mvnw spring-boot:run -pl application -Dspring-boot.run.profiles=postgres
```

## Available Endpoints

### Patients API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/patients | Get all patients |
| GET | /api/v1/patients/{id} | Get patient by ID |
| GET | /api/v1/patients?ownerId={id} | Get patients by owner |
| POST | /api/v1/patients | Create patient |
| PUT | /api/v1/patients/{id} | Update patient |
| DELETE | /api/v1/patients/{id} | Delete patient |

### Clients API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/clients | Get all clients |
| GET | /api/v1/clients/{id} | Get client by ID |
| POST | /api/v1/clients | Create client |
| PUT | /api/v1/clients/{id} | Update client |
| DELETE | /api/v1/clients/{id} | Delete client |

## Development

### Build & Test

```bash
cd backend

# Build all modules
./mvnw clean install

# Run tests only
./mvnw test

# Run with quality checks (CI mode)
./mvnw clean verify -Pci

# Skip quality checks (fast mode)
./mvnw clean install -Pfast
```

### Code Formatting

```bash
# Check formatting
./mvnw spotless:check

# Apply formatting
./mvnw spotless:apply
```

### Running Quality Checks

```bash
# Checkstyle
./mvnw checkstyle:check

# SpotBugs
./mvnw spotbugs:check

# All checks together
./mvnw verify -Pci
```

## Project Structure

```
vet-clinic/
├── docker-compose.yml          # PostgreSQL for development
├── README.md
├── Makefile                    # Common commands
│
└── backend/
    ├── pom.xml                 # Parent POM
    ├── checkstyle.xml          # Code style rules
    ├── lombok.config           # Lombok configuration
    │
    ├── common/                 # Shared utilities
    │   └── src/main/java/com/vetclinic/common/
    │       ├── api/            # API response classes
    │       └── domain/         # Base entity
    │
    ├── patient-module/         # Patient domain
    │   └── src/main/java/com/vetclinic/patient/
    │       ├── api/            # REST controller, DTOs
    │       ├── domain/         # Service, entities, ports
    │       └── infrastructure/ # JPA repository
    │
    ├── client-module/          # Client domain
    │   └── src/main/java/com/vetclinic/client/
    │       ├── api/
    │       ├── domain/
    │       └── infrastructure/
    │
    └── application/            # Main Spring Boot app
        └── src/main/java/com/vetclinic/
            ├── VetClinicApplication.java
            └── config/         # Global config
```

## Architecture

Each module follows **Hexagonal Architecture**:

```
┌─────────────────────────────────────────────────┐
│                      API                         │
│         (Controllers, DTOs, Mappers)            │
└─────────────────────┬───────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│                    DOMAIN                        │
│     (Services, Entities, Ports/Interfaces)      │
└─────────────────────┬───────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│               INFRASTRUCTURE                     │
│         (JPA Repositories, Adapters)            │
└─────────────────────────────────────────────────┘
```

### Module Boundaries

Modules are isolated using **ArchUnit tests** that enforce:
- Domain cannot depend on Infrastructure
- Domain cannot depend on API
- Modules cannot directly depend on each other's internals

## Quality Tools

| Tool | Purpose |
|------|---------|
| Spotless | Code formatting (Google Java Format) |
| Checkstyle | Code style enforcement |
| SpotBugs | Static bug analysis |
| JaCoCo | Code coverage (80% minimum) |
| ArchUnit | Architecture enforcement |

## Example Requests

### Create a Client
```bash
curl -X POST http://localhost:8080/api/v1/clients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890"
  }'
```

### Create a Patient
```bash
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Buddy",
    "species": "DOG",
    "breed": "Golden Retriever",
    "dateOfBirth": "2020-05-15",
    "ownerId": "CLIENT_UUID_HERE"
  }'
```

## IntelliJ IDEA Setup

1. Open the project (File → Open → select `vet-clinic` folder)
2. Import Maven project when prompted
3. Use Run Configurations:
   - **Backend (H2)**: Run `VetClinicApplication` with no extra config
   - **Backend (PostgreSQL)**: Add VM option `-Dspring.profiles.active=postgres`

## Troubleshooting

### Port 5432 already in use
```bash
docker-compose down
docker-compose up -d
```

### Maven build fails
```bash
./mvnw clean install -DskipTests -Pfast
```

### Reset database
```bash
docker-compose down -v
docker-compose up -d
```
