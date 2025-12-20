# Troubleshooting Guide

Common issues and their solutions for this Windows development environment.

## Maven Issues

### Issue: "JAVA_HOME not set"

**Error**:
```
Error: JAVA_HOME is not defined correctly.
```

**Solution**:
Always set JAVA_HOME before running Maven commands:
```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean compile
```

### Issue: "mvn: command not found"

**Error**:
```
bash: mvn: command not found
```

**Solution**:
Use the full path to Maven wrapper:
```bash
/c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn
```

### Issue: Spotless Check Fails

**Error**:
```
[ERROR] Execution default of goal com.diffplug.spotless:spotless-maven-plugin:2.43.0:check failed
```

**Solution**:
Apply Spotless formatting:
```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  spotless:apply -q \
  -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

Or skip checks during development:
```bash
mvn clean install -Pfast
```

### Issue: Checkstyle Violations

**Error**:
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-checkstyle-plugin:3.4.0:check
```

**Solution**:
Review and fix checkstyle violations, or use `-Pfast` profile to skip:
```bash
mvn clean install -Pfast
```

### Issue: Tests Fail with "Cannot create container"

**Error**:
```
Could not start container
```

**Solution**:
1. Ensure Docker Desktop is running
2. Check Docker status:
   ```bash
   docker ps
   ```
3. Restart Docker Desktop if needed
4. Run tests again

## Database Issues

### Issue: "Connection refused" to PostgreSQL

**Error**:
```
Connection refused: localhost:5432
```

**Solution**:
1. Check if PostgreSQL container is running:
   ```bash
   docker ps | grep postgres
   ```

2. Start PostgreSQL if not running:
   ```bash
   cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
   docker-compose up -d postgres
   ```

3. Wait for PostgreSQL to be ready:
   ```bash
   docker-compose logs -f postgres
   ```

4. Verify connection:
   ```bash
   docker exec vetclinic-db pg_isready -U vetclinic
   ```

### Issue: "Database does not exist"

**Error**:
```
FATAL: database "vetclinic" does not exist
```

**Solution**:
Recreate the database by removing and recreating the container:
```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose down -v
docker-compose up -d
```

Warning: This will delete all data in the database!

### Issue: Migration Failures

**Error**:
```
FlywayException: Validate failed: Migration checksum mismatch
```

**Solution**:
1. For development, reset the database:
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```

2. For production, repair Flyway:
   ```bash
   mvn flyway:repair
   ```

## Keycloak Issues

### Issue: "Unable to connect to Keycloak"

**Error**:
```
Connection refused: localhost:8180
```

**Solution**:
1. Check Keycloak status:
   ```bash
   docker ps | grep keycloak
   ```

2. Start Keycloak:
   ```bash
   cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
   docker-compose up -d keycloak
   ```

3. Wait for Keycloak to start (takes ~30-60 seconds):
   ```bash
   docker-compose logs -f keycloak
   ```

4. Check health:
   ```bash
   curl http://localhost:8180/health/ready
   ```

### Issue: "Invalid token"

**Error**:
```
401 Unauthorized
```

**Solution**:
1. Get a fresh token:
   ```bash
   curl -X POST http://localhost:8080/auth/token \
     -H "Content-Type: application/json" \
     -d '{"username": "user", "password": "user"}'
   ```

2. Use the new token in requests:
   ```bash
   TOKEN="new-token-here"
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/patients
   ```

Tokens expire after 30 minutes (1800 seconds).

### Issue: "Realm not found"

**Error**:
```
Realm 'vetclinic' not found
```

**Solution**:
Recreate Keycloak with realm import:
```bash
cd "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp"
docker-compose down
docker-compose up -d keycloak
```

Ensure `docker/keycloak/vetclinic-realm.json` exists.

## Port Conflicts

### Issue: "Port 8080 already in use"

**Error**:
```
Web server failed to start. Port 8080 was already in use.
```

**Solution**:
1. Find the process using port 8080:
   ```bash
   netstat -ano | findstr :8080
   ```

2. Kill the process (replace PID):
   ```bash
   taskkill /PID <PID> /F
   ```

3. Or kill all Java processes:
   ```powershell
   powershell -Command "Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force"
   ```

### Issue: "Port 5432 already in use"

**Solution**:
Another PostgreSQL instance is running. Stop it:
1. Check running containers:
   ```bash
   docker ps
   ```

2. Stop the conflicting container:
   ```bash
   docker stop <container-id>
   ```

3. Or stop all Docker Compose services:
   ```bash
   docker-compose down
   ```

## Docker Issues

### Issue: "Docker daemon not running"

**Error**:
```
Cannot connect to the Docker daemon
```

**Solution**:
1. Open Docker Desktop
2. Wait for Docker to start (check system tray icon)
3. Verify Docker is running:
   ```bash
   docker ps
   ```

### Issue: "No space left on device"

**Error**:
```
no space left on device
```

**Solution**:
Clean up Docker resources:
```bash
# Remove stopped containers
docker container prune -f

# Remove unused images
docker image prune -a -f

# Remove unused volumes
docker volume prune -f

# Remove unused networks
docker network prune -f

# Or clean everything at once
docker system prune -a --volumes -f
```

### Issue: Container keeps restarting

**Solution**:
1. Check container logs:
   ```bash
   docker-compose logs <service-name>
   ```

2. For backend:
   ```bash
   docker-compose logs backend
   ```

3. Look for error messages and fix the underlying issue

## Build Issues

### Issue: "Out of memory" during build

**Error**:
```
Java heap space
OutOfMemoryError
```

**Solution**:
Increase Maven memory:
```bash
export MAVEN_OPTS="-Xmx2048m"
```

Then run Maven command.

### Issue: Compilation fails with MapStruct errors

**Error**:
```
No implementation found for [mapper method]
```

**Solution**:
1. Clean and rebuild:
   ```bash
   mvn clean compile
   ```

2. Ensure annotation processors are configured in pom.xml

3. Check MapStruct mapper interfaces are correct

### Issue: Tests pass locally but fail in CI

**Solution**:
Run with CI profile locally:
```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean verify -Pci
```

This runs all quality checks that CI runs.

## Application Runtime Issues

### Issue: Application starts but endpoints return 404

**Solution**:
1. Check application started successfully:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. Verify endpoint paths in controllers

3. Check Spring Boot logs for mapping information:
   ```
   Mapped "{[/api/v1/patients]}"
   ```

### Issue: "Whitelabel Error Page"

**Solution**:
This usually means:
1. Endpoint doesn't exist - check the URL
2. Exception occurred - check application logs
3. Security blocked the request - check if authentication is required

### Issue: JSON parsing errors

**Error**:
```
JSON parse error: Unexpected character
```

**Solution**:
1. Validate JSON syntax with a JSON validator
2. Ensure Content-Type header is set:
   ```bash
   curl -X POST http://localhost:8080/api/v1/patients \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TOKEN" \
     -d '{"name": "Fluffy", "species": "Cat"}'
   ```

## Performance Issues

### Issue: Application starts slowly

**Possible causes**:
1. Database connection timeout - ensure PostgreSQL is running
2. Keycloak not responding - ensure Keycloak is ready
3. First-time dependency download - wait for completion

**Solution**:
1. Start dependencies first:
   ```bash
   docker-compose up -d postgres keycloak
   ```

2. Wait for health checks to pass

3. Then start the application

### Issue: Tests run slowly

**Solution**:
1. Use `-Pfast` profile to skip quality checks:
   ```bash
   mvn test -Pfast
   ```

2. Run specific test classes instead of all tests:
   ```bash
   mvn test -Dtest=PatientServiceTest -Pfast
   ```

3. Ensure Docker has enough resources (4GB+ RAM recommended)

## Windows-Specific Issues

### Issue: Line ending issues in Git

**Solution**:
Configure Git to handle line endings:
```bash
git config --global core.autocrlf true
```

### Issue: Path too long

**Error**:
```
Filename or extension is too long
```

**Solution**:
1. Move project closer to root drive:
   ```
   C:\Projects\klinikaxp
   ```

2. Or enable long paths in Windows:
   - Run as Administrator: `gpedit.msc`
   - Navigate to: Computer Configuration > Administrative Templates > System > Filesystem
   - Enable "Enable Win32 long paths"

### Issue: Permission denied on shell scripts

**Solution**:
Use Git Bash instead of CMD/PowerShell, or use .cmd equivalents.

## Getting Help

If you encounter an issue not listed here:

1. Check application logs in `logs/` directory
2. Check Docker logs: `docker-compose logs`
3. Check GitHub Issues for similar problems
4. Enable debug logging in `application.yml`:
   ```yaml
   logging:
     level:
       com.vetclinic: DEBUG
   ```

## Preventive Measures

To avoid common issues:

1. Always use `-Pfast` during development
2. Run `spotless:apply` before committing
3. Keep Docker Desktop running during development
4. Set JAVA_HOME in your shell profile
5. Run `docker-compose up -d` before starting the backend
6. Commit often and keep commits small
7. Run `mvn clean` when switching branches
