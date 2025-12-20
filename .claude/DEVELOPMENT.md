# Local Development Setup Guide

This document contains all the essential information for developing on this Windows machine.

## Project Overview

**Project**: Veterinary Clinic Management System
**Location**: `C:\Users\piotr\IdeaProjects\drag-n-drop\klinikaxp`
**Technology Stack**: Java 21 + Spring Boot 3.3.x + PostgreSQL + Keycloak + React/TypeScript

## Directory Structure

```
klinikaxp/
├── backend/           # Spring Boot multi-module Maven project
│   ├── common/        # Shared utilities and base classes
│   ├── patient-module/# Patient domain module
│   ├── client-module/ # Client domain module
│   ├── application/   # Main Spring Boot application
│   └── pom.xml        # Parent POM
├── docker/            # Docker configuration files
├── docker-compose.yml # Local development services
└── .claude/           # Claude Code documentation
```

## Java Setup

**Java Version**: Java 21
**Java Location**: `C:\Users\piotr\.jdks\ms-21.0.7`

### Setting JAVA_HOME

For all Maven commands, you MUST set JAVA_HOME:

```bash
# Git Bash / Unix-style shell
export JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7"

# Or inline for single command
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" mvn [goal]
```

### Verify Java Version

```bash
"/c/Users/piotr/.jdks/ms-21.0.7/bin/java.exe" -version
```

## Maven Setup

**Maven Wrapper Location**: `/c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn`

### Standard Maven Command Format

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  [goal] \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Common Maven Goals

```bash
# Compile the project
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean compile \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Run tests
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  test \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Package the application
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean package \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Apply code formatting
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spotless:apply -q \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Maven Profiles

#### Fast Profile (-Pfast)
Skips quality checks for faster development:
- Skips Spotless formatting check
- Skips Checkstyle
- Skips SpotBugs
- Skips JaCoCo code coverage

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean install -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

#### CI Profile (-Pci)
Runs all quality checks (used in CI/CD):
- Checkstyle validation
- SpotBugs analysis
- Code coverage checks

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean verify -Pci \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Module-Specific Commands

```bash
# Build only the application module and its dependencies
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean install -pl application -am -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# -pl: Project List (specific module)
# -am: Also Make (build dependencies)
```

## Docker Services

### Docker Compose Configuration

File: `C:\Users\piotr\IdeaProjects\drag-n-drop\klinikaxp\docker-compose.yml`

**Services**:
1. **PostgreSQL** (port 5432)
   - Database: vetclinic
   - User: vetclinic
   - Password: vetclinic (default)

2. **Keycloak** (port 8180)
   - Admin user: admin
   - Admin password: admin
   - Realm: vetclinic

3. **Backend** (port 8080) - Optional, requires `--profile full`
   - Spring Boot application
   - Profile: docker

### Starting Services

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"

# Start only PostgreSQL and Keycloak
docker-compose up -d

# Start all services including backend
docker-compose --profile full up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Service Health Checks

```bash
# Check running containers
docker ps

# Check PostgreSQL
docker exec vetclinic-db pg_isready -U vetclinic

# Check Keycloak
curl http://localhost:8180/health/ready
```

## Running the Backend

### Option 1: Using Maven Spring Boot Plugin

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spring-boot:run -pl application -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Option 2: Running the JAR

```bash
# First, build the JAR
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean package -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Then run it
"/c/Users/piotr/.jdks/ms-21.0.7/bin/java.exe" \
  -jar "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/application/target/application-0.0.1-SNAPSHOT.jar"
```

**Backend runs on**: http://localhost:8080

## Authentication & Testing

### Get Authentication Token

```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user"}'
```

Response:
```json
{
  "token": "eyJhbGci...",
  "expiresIn": 1800
}
```

### Test Authenticated Endpoint

```bash
# Set the token
TOKEN="your-token-here"

# Make authenticated request
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/patients
```

### Default Test Users

From Keycloak realm configuration:
- **Username**: user
- **Password**: user
- **Role**: user

## Running Tests

### All Tests

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  test -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Specific Test Class

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  test -Dtest=PatientApiIntegrationTest -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Integration Tests

Integration tests use **Testcontainers** with PostgreSQL:
- Automatically starts PostgreSQL container
- Runs migrations with Flyway
- Cleans up after tests

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  verify -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

## Code Quality Tools

### Spotless (Code Formatting)

```bash
# Check formatting
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spotless:check \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Apply formatting
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spotless:apply -q \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

Format: Google Java Format (AOSP style)

### Checkstyle

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  checkstyle:check \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

Config: `checkstyle.xml` in project root

### SpotBugs

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spotbugs:check \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### JaCoCo (Code Coverage)

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  jacoco:report \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

Report location: `target/site/jacoco/index.html`

## Common Development Workflows

### 1. Start Development Environment

```bash
# 1. Start Docker services
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose up -d

# 2. Wait for services to be ready
docker-compose logs -f

# 3. Build and run backend
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spring-boot:run -pl application -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### 2. Run Tests Before Commit

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean verify -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### 3. Full CI-like Build

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean verify -Pci \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

## Troubleshooting

### Maven Can't Find Java

Ensure JAVA_HOME is set:
```bash
echo $JAVA_HOME  # Should show: /c/Users/piotr/.jdks/ms-21.0.7
```

### Backend Can't Connect to Database

1. Check PostgreSQL is running:
   ```bash
   docker ps | grep postgres
   ```

2. Check connection:
   ```bash
   docker exec vetclinic-db pg_isready -U vetclinic
   ```

### Keycloak Not Accessible

1. Check Keycloak is running:
   ```bash
   docker ps | grep keycloak
   ```

2. Check health:
   ```bash
   curl http://localhost:8180/health/ready
   ```

### Port Already in Use

Find process using port:
```bash
# Find process on port 8080
netstat -ano | findstr :8080

# Kill process (replace PID)
taskkill /PID <PID> /F
```

### Tests Failing with Testcontainers

Ensure Docker is running:
```bash
docker ps
```

Testcontainers requires Docker to start containers for integration tests.

## Environment Variables

### Required for Maven

- `JAVA_HOME`: `/c/Users/piotr/.jdks/ms-21.0.7`

### Optional for Docker

- `DB_PASSWORD`: PostgreSQL password (default: vetclinic)

## Useful URLs

- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Keycloak Admin: http://localhost:8180
- PostgreSQL: localhost:5432

## Notes

- Always use the `-Pfast` profile for local development to skip time-consuming checks
- Use `-Pci` profile before pushing to ensure all checks pass
- Integration tests require Docker to be running
- The backend expects PostgreSQL and Keycloak to be available
- Default Spring profile is `default`, use `docker` profile when running in containers
