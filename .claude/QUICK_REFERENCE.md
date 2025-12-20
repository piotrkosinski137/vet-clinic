# Quick Reference Guide

Quick commands for common development tasks on this Windows machine.

## Environment Setup

```bash
# Java Home
export JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7"

# Maven location
MVN="/c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn"

# Project root
PROJECT_ROOT="C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
BACKEND_POM="$PROJECT_ROOT/backend/pom.xml"
```

## Common Maven Commands

```bash
# Compile
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean compile -Pfast -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Run tests
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  test -Pfast -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Run specific test
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  test -Dtest=TestClassName -Pfast -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Package
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean package -Pfast -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Format code
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spotless:apply -q -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Full CI build
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean verify -Pci -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

## Docker Commands

```bash
# Start services (PostgreSQL + Keycloak)
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose up -d

# Start all services including backend
docker-compose --profile full up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f

# Check status
docker ps
```

## Run Backend

```bash
# Using Maven
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spring-boot:run -pl application -Pfast -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"

# Using JAR (after package)
"/c/Users/piotr/.jdks/ms-21.0.7/bin/java.exe" \
  -jar "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/application/target/application-0.0.1-SNAPSHOT.jar"
```

## Authentication

```bash
# Get token
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user"}'

# Use token
TOKEN="your-token-here"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/patients
```

## Maven Profiles

- `-Pfast` - Skip quality checks (development)
- `-Pci` - Run all quality checks (before commit)

## Ports

- Backend: 8080
- PostgreSQL: 5432
- Keycloak: 8180

## Key Paths

- Java: `/c/Users/piotr/.jdks/ms-21.0.7`
- Maven: `/c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn`
- Project: `C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp`
- Backend POM: `C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml`
