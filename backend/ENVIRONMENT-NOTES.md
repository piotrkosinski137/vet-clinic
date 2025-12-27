# Environment Notes & Learnings

## Working Commands

### Maven (Windows/Git Bash)
```bash
# Always use this pattern - works reliably
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn <command>

# Examples:
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn test -pl application -Pfast
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn clean compile -Pfast
```

### Java JAR Execution
```bash
"/c/Users/piotr/.jdks/ms-21.0.7/bin/java.exe" -jar "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/application/target/application-0.0.1-SNAPSHOT.jar"
```

### Docker
```bash
docker-compose up -d                    # Start all services
docker ps                               # Check running containers
docker exec vetclinic-db psql -U vetclinic -d vetclinic -c "SELECT 1;"  # Test DB connection
```

## Key Ports
- **Backend**: 8080
- **Frontend**: 3000
- **PostgreSQL**: 5436 (mapped from container's 5432)
- **Keycloak**: 8180

## PostgreSQL Specifics (vs H2)

### JPQL Query Fixes
H2 auto-converts parameters, PostgreSQL doesn't. Use CAST for string parameters:
```java
// WRONG - causes "function lower(bytea) does not exist"
WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))

// CORRECT
WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', CAST(:firstName AS string), '%'))
```

### Native Queries for PostgreSQL-Specific Features
PostgreSQL INTERVAL not supported in JPQL - use native query:
```java
@Query(value = "SELECT ... WHERE v.visit_date + (v.duration_minutes * INTERVAL '1 minute') > :startTime", nativeQuery = true)
```

## Testcontainers

### Singleton Container Pattern (Required)
Don't use `@Container` annotation - use static block:
```java
protected static final PostgreSQLContainer<?> postgresContainer;
static {
    postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    postgresContainer.start();
}
```

### Test Configuration
`application-test.yml` needs:
```yaml
spring:
  flyway:
    clean-on-validation-error: true
    clean-disabled: false
```

## Multi-Tenancy in Tests
Tests need TenantContext set. Add filter in TestSecurityConfig:
```java
@Bean
@Order(0)
public Filter testTenantFilter() {
    return (request, response, chain) -> {
        try {
            TenantContext.setCurrentClinicId(TEST_CLINIC_ID);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    };
}
```

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| `./mvnw` not found | Use full Maven path with JAVA_HOME |
| `lower(bytea) does not exist` | Add CAST(:param AS string) in JPQL |
| Container not shared across tests | Use Singleton Container Pattern |
| TenantContext not set | Add testTenantFilter bean |
| Flyway validation error | Enable clean-on-validation-error |
| Invoice NullPointerException | Calculate item totals before summing |
