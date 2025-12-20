# Vet Clinic Application

A modular monolithic veterinary clinic management system built with Java 21, Spring Boot 3.3, and React.

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Docker & Docker Compose
- Node.js 20+

## Quick Start (Full Stack with Auth)

### 1. Start Keycloak & PostgreSQL

```bash
docker-compose up -d
```

Wait ~30 seconds for Keycloak to start.

### 2. Start Backend

```bash
cd backend
mvn spring-boot:run -pl application
```

### 3. Start Frontend (new terminal)

```bash
cd frontend
npm install
npm run dev
```

### 4. Access

- **Frontend**: http://localhost:3000
- **Keycloak Admin**: http://localhost:8180 (admin/admin)
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### 5. Login

- **Username**: `user`
- **Password**: `user`

## Authentication

### Keycloak Setup

Keycloak is pre-configured with:
- **Realm**: `vetclinic`
- **Users**: `user/user` and `admin/admin`
- **Token lifetime**: 30 days (for easy testing)

### Get Token for API Testing

```bash
# Get token
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user"}'

# Use token in requests
curl http://localhost:8080/api/v1/patients \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Direct Keycloak Token (alternative)

```bash
curl -X POST http://localhost:8180/realms/vetclinic/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=vetclinic-app" \
  -d "client_secret=vetclinic-secret" \
  -d "username=user" \
  -d "password=user"
```

## Backend Only

### Option 1: Run with H2 (In-Memory Database)

```bash
cd backend
./mvnw spring-boot:run -pl application
```

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

## API Client Generation

The frontend uses **OpenAPI Generator** to auto-generate TypeScript API client from the backend's OpenAPI spec. This keeps frontend/backend types in sync.

```bash
# 1. Backend must be running
cd backend && mvn spring-boot:run -pl application

# 2. Generate API client (new terminal)
cd frontend
npm run api:generate
```

This creates typed client in `frontend/src/api/generated/`. Regenerate after backend API changes.

## Project Structure

```
vet-clinic/
├── docker-compose.yml          # PostgreSQL for development
├── README.md
├── Makefile                    # Common commands
│
├── frontend/                   # React + TypeScript + Vite
│   ├── src/
│   │   ├── api/                # API client (manual + generated)
│   │   ├── pages/              # Page components
│   │   └── hooks/              # Custom hooks
│   ├── openapitools.json       # OpenAPI Generator config
│   └── package.json
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

All API requests require authentication. Get a token first:

```bash
# Get token and extract access_token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user"}' | jq -r '.access_token')
```

### Create a Client
```bash
curl -X POST http://localhost:8080/api/v1/clients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
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
  -H "Authorization: Bearer $TOKEN" \
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
