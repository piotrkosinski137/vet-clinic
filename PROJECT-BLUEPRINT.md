# Veterinary Clinic Application - Project Blueprint

## Overview

A monolithic but modular Java/Spring Boot application with React frontend for managing veterinary clinics.

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 (LTS) | Core language |
| Spring Boot | 3.3.x | Application framework |
| Maven | 3.9.x | Build tool & dependency management |
| JUnit 5 | 5.10.x | Unit & integration testing |
| Database | TBD (PostgreSQL recommended) | Data persistence |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.x | UI framework |
| TypeScript | 5.x | Type-safe JavaScript |
| Vite | 5.x | Build tool (faster than CRA) |
| React Query | 5.x | Server state management |
| React Router | 6.x | Routing |

---

## Project Structure

```
vet-clinic/
├── docker-compose.yml              # Root level for easy startup
├── docker-compose.dev.yml          # Development overrides
├── .env.example                    # Environment template
├── README.md                       # Quick start guide
├── Makefile                        # Common commands shortcuts
│
├── backend/
│   ├── pom.xml                     # Parent POM
│   ├── checkstyle.xml              # Code style rules
│   ├── spotless-config.xml         # Spotless configuration
│   │
│   ├── application/                # Main Spring Boot app (assembles modules)
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/.../VetClinicApplication.java
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       ├── application-dev.yml
│   │       │       └── application-prod.yml
│   │       └── test/
│   │
│   ├── common/                     # Shared utilities (minimal!)
│   │   ├── pom.xml
│   │   └── src/
│   │
│   ├── patient-module/             # Patient/Animal management
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/java/.../patient/
│   │       │   ├── api/            # REST controllers
│   │       │   ├── domain/         # Business logic & entities
│   │       │   ├── infrastructure/ # Repository implementations
│   │       │   └── PatientModuleConfig.java
│   │       └── test/
│   │
│   ├── appointment-module/         # Scheduling & appointments
│   ├── client-module/              # Pet owners management
│   ├── medical-record-module/      # Medical history, treatments
│   ├── inventory-module/           # Medicines, supplies
│   ├── billing-module/             # Invoices, payments
│   └── notification-module/        # Email, SMS reminders
│
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── src/
│       ├── api/                    # API client (generated from OpenAPI)
│       ├── components/
│       ├── features/               # Feature-based organization
│       ├── hooks/
│       └── utils/
│
├── api-scenarios/                  # API test scenarios
│   ├── patients.json
│   ├── appointments.json
│   └── full-workflow.json
│
└── docs/
    ├── architecture.md
    ├── module-guidelines.md
    └── api/                        # Generated Swagger docs
```

---

## Module Architecture & Decoupling Safeguards

### Module Structure (Hexagonal/Clean Architecture per module)

```
module-name/
├── api/                    # Inbound adapters (REST controllers)
│   ├── dto/                # Request/Response DTOs
│   └── mapper/             # DTO <-> Domain mappers
├── domain/                 # Core business logic (NO framework dependencies)
│   ├── model/              # Domain entities & value objects
│   ├── port/               # Interfaces (inbound & outbound)
│   ├── service/            # Domain services
│   └── event/              # Domain events
├── infrastructure/         # Outbound adapters
│   ├── persistence/        # JPA entities & repositories
│   └── client/             # External service clients
└── ModuleConfig.java       # Spring configuration for this module
```

### Decoupling Safeguards

#### 1. ArchUnit Tests (Mandatory)
```xml
<!-- In each module's pom.xml -->
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <scope>test</scope>
</dependency>
```

```java
// ArchitectureTest.java in each module
@AnalyzeClasses(packages = "com.vetclinic.patient")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_should_not_use_spring =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule modules_should_not_access_other_modules_internals =
        noClasses()
            .that().resideInAPackage("..patient..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..appointment..domain..",
                "..appointment..infrastructure.."
            );
}
```

#### 2. Inter-Module Communication Rules
- Modules communicate ONLY via:
  - **Public API interfaces** (defined in `domain/port/`)
  - **Domain Events** (async, loosely coupled)
  - **REST calls** (for future microservice extraction)
- NO direct entity references between modules
- Shared only: IDs (as value objects), common DTOs in `common` module

#### 3. Maven Enforcer Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <executions>
        <execution>
            <id>enforce-module-boundaries</id>
            <goals><goal>enforce</goal></goals>
            <configuration>
                <rules>
                    <bannedDependencies>
                        <excludes>
                            <!-- Prevent circular dependencies -->
                        </excludes>
                    </bannedDependencies>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### 4. Module Public API Pattern
```java
// Each module exposes ONLY this interface to other modules
public interface PatientModuleApi {
    PatientBasicInfo getPatientBasicInfo(PatientId patientId);
    boolean existsById(PatientId patientId);
}
```

---

## Quality & Code Standards

### Testing Strategy (~100% Feature Coverage)

| Test Type | Tool | Coverage Target |
|-----------|------|-----------------|
| Unit Tests | JUnit 5 + Mockito | All domain logic |
| Integration Tests | @SpringBootTest + Testcontainers | All repositories & APIs |
| Contract Tests | Spring Cloud Contract / Pact | Module boundaries |
| E2E Tests | Playwright | Critical user flows |

```xml
<!-- JaCoCo for coverage enforcement -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.90</minimum>
                    </limit>
                    <limit>
                        <counter>BRANCH</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.85</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

### Code Quality Tools

#### Spotless (Code Formatting)
```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.43.0</version>
    <configuration>
        <java>
            <googleJavaFormat>
                <version>1.19.1</version>
                <style>AOSP</style>
            </googleJavaFormat>
            <removeUnusedImports/>
            <importOrder>
                <order>java,javax,org,com,com.vetclinic</order>
            </importOrder>
        </java>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
            <phase>validate</phase>
        </execution>
    </executions>
</plugin>
```

#### Other Quality Plugins
```xml
<!-- Error Prone - catches common bugs -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>-XDcompilePolicy=simple</arg>
            <arg>-Xplugin:ErrorProne</arg>
        </compilerArgs>
        <annotationProcessorPaths>
            <path>
                <groupId>com.google.errorprone</groupId>
                <artifactId>error_prone_core</artifactId>
                <version>2.24.1</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>

<!-- SpotBugs - static analysis -->
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.3.0</version>
</plugin>

<!-- Checkstyle -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
        <failOnViolation>true</failOnViolation>
    </configuration>
</plugin>
```

---

## API Documentation

### Swagger/OpenAPI Setup
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

```yaml
# application.yml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
  show-actuator: true
```

### API Scenario Files - Postman Collection Format (api-scenarios/)

The API scenarios are in **Postman Collection v2.1 format** for easy import.

```json
// api-scenarios/VetClinic.postman_collection.json
{
  "info": {
    "name": "Vet Clinic API",
    "_postman_id": "vet-clinic-collection",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    { "key": "baseUrl", "value": "http://localhost:8080/api/v1" },
    { "key": "accessToken", "value": "" },
    { "key": "patientId", "value": "" },
    { "key": "ownerId", "value": "" }
  ],
  "auth": {
    "type": "bearer",
    "bearer": [{ "key": "token", "value": "{{accessToken}}" }]
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Login (Get Token)",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set('accessToken', jsonData.access_token);"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "url": "{{keycloakUrl}}/realms/vetclinic/protocol/openid-connect/token",
            "header": [
              { "key": "Content-Type", "value": "application/x-www-form-urlencoded" }
            ],
            "body": {
              "mode": "urlencoded",
              "urlencoded": [
                { "key": "grant_type", "value": "password" },
                { "key": "client_id", "value": "vetclinic-app" },
                { "key": "username", "value": "{{username}}" },
                { "key": "password", "value": "{{password}}" }
              ]
            }
          }
        }
      ]
    },
    {
      "name": "Patients",
      "item": [
        {
          "name": "Create Patient",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status is 201', () => pm.response.to.have.status(201));",
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set('patientId', jsonData.id);"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/patients",
            "header": [
              { "key": "Content-Type", "value": "application/json" }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Buddy\",\n  \"species\": \"DOG\",\n  \"breed\": \"Golden Retriever\",\n  \"dateOfBirth\": \"2020-05-15\",\n  \"ownerId\": \"{{ownerId}}\"\n}"
            }
          }
        },
        {
          "name": "Get Patient by ID",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/patients/{{patientId}}"
          }
        },
        {
          "name": "Search Patients by Owner",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/patients?ownerId={{ownerId}}",
              "query": [{ "key": "ownerId", "value": "{{ownerId}}" }]
            }
          }
        },
        {
          "name": "Update Patient",
          "request": {
            "method": "PUT",
            "url": "{{baseUrl}}/patients/{{patientId}}",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Buddy Jr.\",\n  \"weight\": 25.5\n}"
            }
          }
        },
        {
          "name": "Delete Patient",
          "request": {
            "method": "DELETE",
            "url": "{{baseUrl}}/patients/{{patientId}}"
          }
        }
      ]
    },
    {
      "name": "Appointments",
      "item": [
        {
          "name": "Schedule Appointment",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/appointments",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"patientId\": \"{{patientId}}\",\n  \"veterinarianId\": \"{{vetId}}\",\n  \"scheduledAt\": \"2024-12-25T10:00:00\",\n  \"type\": \"CHECKUP\",\n  \"notes\": \"Annual vaccination\"\n}"
            }
          }
        }
      ]
    }
  ]
}
```

```json
// api-scenarios/VetClinic.postman_environment.json
{
  "name": "Vet Clinic - Local",
  "values": [
    { "key": "baseUrl", "value": "http://localhost:8080/api/v1", "enabled": true },
    { "key": "keycloakUrl", "value": "http://localhost:8180", "enabled": true },
    { "key": "username", "value": "testuser", "enabled": true },
    { "key": "password", "value": "testpass", "enabled": true }
  ]
}
```

**Import to Postman:**
1. Open Postman → Import → Upload Files
2. Select `VetClinic.postman_collection.json`
3. Import `VetClinic.postman_environment.json` as environment
4. Select "Vet Clinic - Local" environment from dropdown

---

## Docker Setup

### docker-compose.yml (Root Level)
```yaml
version: '3.8'

services:
  # Database
  postgres:
    image: postgres:16-alpine
    container_name: vetclinic-db
    environment:
      POSTGRES_DB: vetclinic
      POSTGRES_USER: vetclinic
      POSTGRES_PASSWORD: ${DB_PASSWORD:-devpassword}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/init-db.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U vetclinic"]
      interval: 5s
      timeout: 5s
      retries: 5

  # Keycloak Database (separate for isolation)
  keycloak-db:
    image: postgres:16-alpine
    container_name: vetclinic-keycloak-db
    profiles: ["auth"]  # Only starts with --profile auth
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: ${KEYCLOAK_DB_PASSWORD:-keycloakpass}
    volumes:
      - keycloak_db_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U keycloak"]
      interval: 5s
      timeout: 5s
      retries: 5

  # Keycloak Identity Provider
  keycloak:
    image: quay.io/keycloak/keycloak:24.0
    container_name: vetclinic-keycloak
    profiles: ["auth"]  # Only starts with --profile auth
    command: start-dev --import-realm
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://keycloak-db:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: ${KEYCLOAK_DB_PASSWORD:-keycloakpass}
      KC_HOSTNAME: localhost
      KC_HTTP_PORT: 8180
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN:-admin}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD:-admin}
    ports:
      - "8180:8180"
    volumes:
      - ./docker/keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json
    depends_on:
      keycloak-db:
        condition: service_healthy

  # Backend
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: vetclinic-backend
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/vetclinic
      SPRING_DATASOURCE_USERNAME: vetclinic
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-devpassword}
      # Keycloak config (used when auth profile is active)
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8180/realms/vetclinic
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy

  # Frontend
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: vetclinic-frontend
    environment:
      VITE_KEYCLOAK_URL: ${KEYCLOAK_URL:-http://localhost:8180}
      VITE_KEYCLOAK_REALM: vetclinic
      VITE_KEYCLOAK_CLIENT_ID: vetclinic-frontend
      VITE_AUTH_ENABLED: ${AUTH_ENABLED:-false}
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  postgres_data:
  keycloak_db_data:
```

### Keycloak Realm Configuration
```json
// docker/keycloak/realm-export.json
{
  "realm": "vetclinic",
  "enabled": true,
  "clients": [
    {
      "clientId": "vetclinic-frontend",
      "enabled": true,
      "publicClient": true,
      "redirectUris": ["http://localhost:3000/*"],
      "webOrigins": ["http://localhost:3000"],
      "standardFlowEnabled": true,
      "directAccessGrantsEnabled": true
    },
    {
      "clientId": "vetclinic-app",
      "enabled": true,
      "publicClient": false,
      "secret": "${KEYCLOAK_CLIENT_SECRET}",
      "serviceAccountsEnabled": true,
      "directAccessGrantsEnabled": true
    }
  ],
  "roles": {
    "realm": [
      { "name": "ADMIN", "description": "Administrator" },
      { "name": "VET", "description": "Veterinarian" },
      { "name": "RECEPTIONIST", "description": "Front desk staff" },
      { "name": "CLIENT", "description": "Pet owner" }
    ]
  },
  "users": [
    {
      "username": "admin",
      "enabled": true,
      "credentials": [{ "type": "password", "value": "admin", "temporary": false }],
      "realmRoles": ["ADMIN"]
    },
    {
      "username": "testuser",
      "enabled": true,
      "credentials": [{ "type": "password", "value": "testpass", "temporary": false }],
      "realmRoles": ["VET"]
    }
  ]
}
```

### Enable/Disable Keycloak

**With Keycloak (auth enabled):**
```bash
docker-compose --profile auth up -d
# Set in .env: AUTH_ENABLED=true
```

**Without Keycloak (no auth, faster startup):**
```bash
docker-compose up -d
# Set in .env: AUTH_ENABLED=false
```

### docker-compose.dev.yml (Development Overrides)
```yaml
version: '3.8'

services:
  postgres:
    ports:
      - "5432:5432"

  # Hot reload for backend
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile.dev
    volumes:
      - ./backend:/app
      - ~/.m2:/root/.m2
    environment:
      SPRING_DEVTOOLS_RESTART_ENABLED: true

  # Hot reload for frontend
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.dev
    volumes:
      - ./frontend:/app
      - /app/node_modules
    environment:
      NODE_ENV: development
```

### backend/Dockerfile
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/application/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Easy Project Setup

### Makefile (Root Level)
```makefile
.PHONY: help setup dev test build clean

help:
	@echo "Available commands:"
	@echo "  make setup    - Initial project setup"
	@echo "  make dev      - Start development environment"
	@echo "  make test     - Run all tests"
	@echo "  make build    - Build production artifacts"
	@echo "  make clean    - Clean all build artifacts"
	@echo "  make format   - Format code with Spotless"
	@echo "  make check    - Run all quality checks"

setup:
	cp .env.example .env
	cd backend && ./mvnw clean install -DskipTests
	cd frontend && npm install
	@echo "Setup complete! Run 'make dev' to start."

dev:
	docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d postgres
	@echo "Waiting for database..."
	@sleep 5
	cd backend && ./mvnw spring-boot:run &
	cd frontend && npm run dev

test:
	cd backend && ./mvnw clean verify
	cd frontend && npm test

build:
	docker-compose build

format:
	cd backend && ./mvnw spotless:apply
	cd frontend && npm run format

check:
	cd backend && ./mvnw spotless:check checkstyle:check spotbugs:check
	cd frontend && npm run lint
```

### .env.example (Committed to Git - Template Only)
```env
# Database
DB_PASSWORD=change_me

# Keycloak (only used with --profile auth)
KEYCLOAK_DB_PASSWORD=change_me
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=change_me
KEYCLOAK_CLIENT_SECRET=change_me
AUTH_ENABLED=false

# Backend
SPRING_PROFILES_ACTIVE=dev

# Frontend
VITE_API_URL=http://localhost:8080/api
VITE_AUTH_ENABLED=false
```

### .env (NOT committed - in .gitignore)
```env
# Actual secrets - copy from .env.example and fill in
DB_PASSWORD=my_actual_secure_password
KEYCLOAK_DB_PASSWORD=my_keycloak_db_password
KEYCLOAK_ADMIN_PASSWORD=my_admin_password
KEYCLOAK_CLIENT_SECRET=generated_secret_here
```

---

## Secrets & Credentials Management

### File Structure
```
vet-clinic/
├── .env.example              # Template (committed)
├── .env                      # Actual secrets (gitignored)
├── .gitignore                # Contains .env, secrets.yml, etc.
│
└── backend/
    └── application/
        └── src/main/resources/
            ├── application.yml           # Main config (committed)
            ├── application-dev.yml       # Dev overrides (committed)
            ├── application-local.yml     # Local dev secrets (gitignored)
            └── application-secrets.yml   # Production secrets (gitignored)
```

### .gitignore (Secrets Section)
```gitignore
# Secrets - NEVER commit these
.env
*.env.local
**/application-local.yml
**/application-secrets.yml
**/secrets.yml
**/*-secrets.yml

# IDE specific secrets
.idea/workspace.xml
*.iws
```

### application.yml (Base Config - Committed)
```yaml
spring:
  application:
    name: vet-clinic

  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/vetclinic}
    username: ${SPRING_DATASOURCE_USERNAME:vetclinic}
    password: ${SPRING_DATASOURCE_PASSWORD:}  # From env or secrets file

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

# Security toggle
app:
  security:
    enabled: ${AUTH_ENABLED:false}

---
# Dev profile
spring:
  config:
    activate:
      on-profile: dev
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

### application-local.yml (Local Secrets - Gitignored)
```yaml
# This file is gitignored - safe for local secrets
spring:
  datasource:
    password: my_local_db_password

keycloak:
  client-secret: local_dev_secret
```

---

## IntelliJ IDEA Run Configurations

### Shared Configs (Committed to Git)
```
.idea/
├── runConfigurations/             # Committed to Git
│   ├── Backend__No_Auth_.xml
│   ├── Backend__With_Keycloak_.xml
│   ├── Frontend_Dev.xml
│   ├── Docker__Full_Stack_.xml
│   ├── Docker__DB_Only_.xml
│   └── All_Tests.xml
└── workspace.xml                  # Gitignored (personal settings)
```

### Backend - No Auth (Run Configuration)
```xml
<!-- .idea/runConfigurations/Backend__No_Auth_.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Backend (No Auth)" type="SpringBootApplicationConfigurationType">
    <module name="application"/>
    <option name="SPRING_BOOT_MAIN_CLASS" value="com.vetclinic.VetClinicApplication"/>
    <option name="ACTIVE_PROFILES" value="dev,local"/>
    <envs>
      <env name="AUTH_ENABLED" value="false"/>
    </envs>
    <option name="WORKING_DIRECTORY" value="$PROJECT_DIR$/backend/application"/>
  </configuration>
</component>
```

### Backend - With Keycloak (Run Configuration)
```xml
<!-- .idea/runConfigurations/Backend__With_Keycloak_.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Backend (With Keycloak)" type="SpringBootApplicationConfigurationType">
    <module name="application"/>
    <option name="SPRING_BOOT_MAIN_CLASS" value="com.vetclinic.VetClinicApplication"/>
    <option name="ACTIVE_PROFILES" value="dev,local,keycloak"/>
    <envs>
      <env name="AUTH_ENABLED" value="true"/>
      <env name="KEYCLOAK_URL" value="http://localhost:8180"/>
    </envs>
    <option name="BEFORE_LAUNCH_TASKS">
      <task type="ShellScript">
        <option name="SCRIPT_PATH" value="$PROJECT_DIR$/scripts/ensure-keycloak.sh"/>
      </task>
    </option>
  </configuration>
</component>
```

### Frontend Dev (Run Configuration)
```xml
<!-- .idea/runConfigurations/Frontend_Dev.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Frontend Dev" type="js.build_tools.npm">
    <package-json value="$PROJECT_DIR$/frontend/package.json"/>
    <command value="run"/>
    <scripts>
      <script value="dev"/>
    </scripts>
    <node-interpreter value="project"/>
  </configuration>
</component>
```

### Docker - DB Only (Run Configuration)
```xml
<!-- .idea/runConfigurations/Docker__DB_Only_.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Docker (DB Only)" type="docker-deploy">
    <deployment type="docker-compose.yml">
      <settings>
        <option name="sourceFilePath" value="docker-compose.yml"/>
        <option name="commandLine" value="up -d postgres"/>
      </settings>
    </deployment>
  </configuration>
</component>
```

### Docker - Full Stack with Auth (Run Configuration)
```xml
<!-- .idea/runConfigurations/Docker__Full_Stack_Auth_.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Docker (Full Stack + Auth)" type="docker-deploy">
    <deployment type="docker-compose.yml">
      <settings>
        <option name="sourceFilePath" value="docker-compose.yml"/>
        <option name="commandLine" value="--profile auth up -d"/>
      </settings>
    </deployment>
  </configuration>
</component>
```

### All Tests (Run Configuration)
```xml
<!-- .idea/runConfigurations/All_Tests.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="All Tests" type="JUnit">
    <module name="backend"/>
    <option name="PACKAGE_NAME" value="com.vetclinic"/>
    <option name="TEST_SEARCH_SCOPE">
      <value defaultName="moduleWithDependencies"/>
    </option>
    <option name="VM_PARAMETERS" value="-Dspring.profiles.active=test"/>
  </configuration>
</component>
```

### Compound Configuration (Start Everything)
```xml
<!-- .idea/runConfigurations/Start_Development.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Start Development" type="CompoundRunConfigurationType">
    <toRun name="Docker (DB Only)" type="docker-deploy"/>
    <toRun name="Backend (No Auth)" type="SpringBootApplicationConfigurationType"/>
    <toRun name="Frontend Dev" type="js.build_tools.npm"/>
  </configuration>
</component>
```

### Usage for New Developer
1. Clone repo
2. Open in IntelliJ IDEA
3. Copy `.env.example` to `.env` and fill in values
4. Copy `backend/.../application-local.yml.example` to `application-local.yml`
5. Run "Start Development" compound configuration
6. Done!

### README.md (Root Level)
```markdown
# Vet Clinic Application

## Quick Start (3 steps)

### Prerequisites
- Java 21
- Node.js 20+
- Docker & Docker Compose

### 1. Clone & Setup
git clone <repo-url>
cd vet-clinic
make setup

### 2. Start Development
make dev

### 3. Access
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Useful Commands
| Command | Description |
|---------|-------------|
| `make dev` | Start development environment |
| `make test` | Run all tests |
| `make format` | Format all code |
| `make check` | Run quality checks |
```

---

## Recommended Dependencies (pom.xml)

```xml
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.3.0</spring-boot.version>
</properties>

<dependencies>
    <!-- Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>

    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.tngtech.archunit</groupId>
        <artifactId>archunit-junit5</artifactId>
        <version>1.2.1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Veterinary Clinic Domain Modules

| Module | Responsibility |
|--------|----------------|
| **patient-module** | Animals/pets CRUD, species, breeds, medical alerts |
| **client-module** | Pet owners, contact info, communication preferences |
| **appointment-module** | Scheduling, calendar, availability, reminders |
| **medical-record-module** | Examinations, diagnoses, treatments, prescriptions, vaccinations |
| **inventory-module** | Medicines, supplies, stock levels, expiry tracking |
| **billing-module** | Invoices, payments, pricing, discounts |
| **notification-module** | Email/SMS for appointments, follow-ups, reminders |

---

## CI/CD Pipeline (GitHub Actions Example)

```yaml
# .github/workflows/ci.yml
name: CI

on: [push, pull_request]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
      - name: Build & Test
        run: cd backend && ./mvnw clean verify
      - name: Quality Checks
        run: cd backend && ./mvnw spotless:check checkstyle:check

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      - run: cd frontend && npm ci
      - run: cd frontend && npm run lint
      - run: cd frontend && npm test
      - run: cd frontend && npm run build
```

---

## Summary Checklist

### Core Stack
- [ ] Java 21 + Spring Boot 3.3.x
- [ ] Maven multi-module structure
- [ ] React 18 + TypeScript + Vite
- [ ] PostgreSQL database (recommended)

### Architecture & Decoupling
- [ ] Hexagonal architecture per module
- [ ] ArchUnit tests for boundary enforcement
- [ ] Module public API pattern
- [ ] Maven Enforcer Plugin for dependency rules

### Testing & Quality
- [ ] JaCoCo coverage (90%+ line, 85%+ branch)
- [ ] Spotless (code formatting)
- [ ] Checkstyle
- [ ] SpotBugs
- [ ] Error Prone
- [ ] Testcontainers for integration tests

### Documentation
- [ ] Swagger/OpenAPI (springdoc)
- [ ] API scenario JSON files (Postman format)

### Authentication
- [ ] Keycloak integration (Docker)
- [ ] Enable/disable auth via profile (`--profile auth`)
- [ ] Realm pre-configured with roles (ADMIN, VET, RECEPTIONIST, CLIENT)

### DevOps & Setup
- [ ] Docker Compose at root level
- [ ] Docker Compose profiles (auth/no-auth)
- [ ] Makefile for common commands
- [ ] GitHub Actions CI pipeline
- [ ] Simple 3-step setup in README

### Configuration & Secrets
- [ ] All configs in YAML files
- [ ] .env.example template (committed)
- [ ] .env with secrets (gitignored)
- [ ] application-local.yml for local secrets (gitignored)

### IDE Support
- [ ] IntelliJ run configurations (committed)
- [ ] Compound "Start Development" configuration
- [ ] Separate configs for auth/no-auth modes
- [ ] Docker run configs in IntelliJ
