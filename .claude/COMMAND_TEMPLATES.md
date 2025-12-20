# Command Templates

Pre-configured command templates for this Windows development environment.

## Environment Variables

```bash
# Set these at the start of your session
export JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7"
export MVN="/c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn"
export PROJECT_ROOT="C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
export BACKEND_POM="$PROJECT_ROOT/backend/pom.xml"
```

## Maven Command Template

Standard template for all Maven commands:

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  <GOAL> \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

Replace `<GOAL>` with the Maven goal (e.g., `clean compile`, `test`, `package`).

## Build Commands

### Clean Build

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean compile -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Full Build (Skip Tests)

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean install -DskipTests -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Package Application

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean package -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

## Test Commands

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
  test -Dtest=<TEST_CLASS_NAME> -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

Replace `<TEST_CLASS_NAME>` with the test class name (e.g., `PatientServiceTest`).

### Specific Test Method

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  test -Dtest=<TEST_CLASS>#<TEST_METHOD> -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

Example: `-Dtest=PatientServiceTest#shouldCreatePatient`

### Integration Tests Only

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  verify -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

## Code Quality Commands

### Format Code (Spotless)

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spotless:apply -q \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Check Formatting

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spotless:check \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Run Checkstyle

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  checkstyle:check \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Run SpotBugs

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spotbugs:check \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Full Quality Check (CI Profile)

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean verify -Pci \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

## Module-Specific Commands

### Build Specific Module with Dependencies

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean install -pl <MODULE_NAME> -am -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

Replace `<MODULE_NAME>` with:
- `common`
- `patient-module`
- `client-module`
- `application`

Example for application module:
```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean install -pl application -am -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

## Run Application Commands

### Spring Boot Run (Development)

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spring-boot:run -pl application -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Run JAR File

First, build the JAR:
```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean package -Pfast \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

Then run it:
```bash
"/c/Users/piotr/.jdks/ms-21.0.7/bin/java.exe" \
  -jar "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/application/target/application-0.0.1-SNAPSHOT.jar"
```

### Run with Specific Profile

```bash
"/c/Users/piotr/.jdks/ms-21.0.7/bin/java.exe" \
  -jar "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/application/target/application-0.0.1-SNAPSHOT.jar" \
  --spring.profiles.active=<PROFILE_NAME>
```

Replace `<PROFILE_NAME>` with `docker`, `test`, etc.

## Docker Commands

### Start All Services

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose up -d
```

### Start Specific Service

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose up -d <SERVICE_NAME>
```

Replace `<SERVICE_NAME>` with:
- `postgres`
- `keycloak`
- `backend` (requires `--profile full`)

### Start with Backend

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose --profile full up -d
```

### View Logs

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose logs -f <SERVICE_NAME>
```

### Stop Services

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose down
```

### Stop and Remove Volumes

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose down -v
```

## Authentication Commands

### Get JWT Token

```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user"}'
```

### Store Token in Variable

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user"}' | \
  python -m json.tool | grep '"token"' | cut -d'"' -f4)
```

### Use Token in Request

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/patients
```

## API Testing Commands

### GET Request

```bash
curl -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  http://localhost:8080/api/v1/<ENDPOINT>
```

### POST Request

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '<JSON_BODY>' \
  http://localhost:8080/api/v1/<ENDPOINT>
```

Example for creating a patient:
```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Fluffy", "species": "Cat", "breed": "Persian", "age": 3}' \
  http://localhost:8080/api/v1/patients
```

### PUT Request

```bash
curl -X PUT \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '<JSON_BODY>' \
  http://localhost:8080/api/v1/<ENDPOINT>/<ID>
```

### DELETE Request

```bash
curl -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/<ENDPOINT>/<ID>
```

## Database Commands

### Connect to PostgreSQL

```bash
docker exec -it vetclinic-db psql -U vetclinic -d vetclinic
```

### Execute SQL Query

```bash
docker exec -it vetclinic-db psql -U vetclinic -d vetclinic -c "<SQL_QUERY>"
```

Example:
```bash
docker exec -it vetclinic-db psql -U vetclinic -d vetclinic -c "SELECT * FROM patients;"
```

### Backup Database

```bash
docker exec vetclinic-db pg_dump -U vetclinic vetclinic > backup.sql
```

### Restore Database

```bash
cat backup.sql | docker exec -i vetclinic-db psql -U vetclinic -d vetclinic
```

## Cleanup Commands

### Clean Maven Build

```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

### Clean Docker Resources

```bash
# Remove all stopped containers
docker container prune -f

# Remove all unused images
docker image prune -a -f

# Remove all unused volumes
docker volume prune -f

# Clean everything
docker system prune -a --volumes -f
```

## Git Commands

### Status

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
git status
```

### Add All Changes

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
git add .
```

### Commit

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
git commit -m "<COMMIT_MESSAGE>"
```

### Push

```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
git push
```

## Process Management

### Find Process on Port

```bash
netstat -ano | findstr :<PORT>
```

### Kill Process by PID

```bash
taskkill /PID <PID> /F
```

### Kill All Java Processes

PowerShell:
```powershell
powershell -Command "Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force"
```

## Health Check Commands

### Backend Health

```bash
curl http://localhost:8080/actuator/health
```

### PostgreSQL Health

```bash
docker exec vetclinic-db pg_isready -U vetclinic
```

### Keycloak Health

```bash
curl http://localhost:8180/health/ready
```

### Check All Services

```bash
docker ps
```

## Useful Aliases

Add these to your `.bashrc` or `.bash_profile`:

```bash
# Java
alias java21='/c/Users/piotr/.jdks/ms-21.0.7/bin/java.exe'

# Maven
alias mvn-vet='JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"'

# Common commands
alias vet-build='mvn-vet clean install -Pfast'
alias vet-test='mvn-vet test -Pfast'
alias vet-run='mvn-vet spring-boot:run -pl application -Pfast'
alias vet-format='mvn-vet spotless:apply -q'

# Docker
alias vet-up='cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp" && docker-compose up -d'
alias vet-down='cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp" && docker-compose down'
alias vet-logs='cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp" && docker-compose logs -f'
```

Then use:
```bash
vet-build
vet-run
vet-up
```
