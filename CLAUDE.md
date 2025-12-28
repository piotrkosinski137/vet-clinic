# Claude Code Guidelines for VetClinic Project

## Rituals

### API Endpoints Documentation
When adding new API endpoints to the backend:
1. Always add the new endpoints to `postman-requests.json`
2. Include proper documentation with description, parameters, and example values
3. Group endpoints under the appropriate collection (Patients, Clients, Visits, etc.)

### Code Standards
- Follow hexagonal architecture (Ports and Adapters pattern)
- Keep domain layer free from API layer dependencies
- Use MapStruct for entity-DTO mapping
- Write unit tests for service methods
- Run `./mvnw spotless:apply` before committing

### Java Code Style
- **ALWAYS use `var` keyword** for local variables when the type is obvious from the right-hand side (Java 10+)
  - Example: `var patient = patientRepository.findById(id)` instead of `Optional<Patient> patient = ...`
  - Example: `var clients = clientRepository.findAll()` instead of `List<Client> clients = ...`
- **ALWAYS import classes and use short names** - Never use fully qualified class names in code
  - Example: `LocalTime.of(9, 0)` NOT `java.time.LocalTime.of(9, 0)`
  - Example: `List.of(...)` NOT `java.util.List.of(...)`
- Use Stream API for collection processing where it improves readability
- Prefer descriptive method names over comments
- Extract complex logic into well-named private methods
- Avoid code duplication - follow DRY principle
- Use records for immutable data objects (DTOs, snapshots)
- Prefer method references over lambdas when possible: `.map(Patient::getId)` over `.map(p -> p.getId())`

### Testing
- Unit tests use Mockito with BDDMockito style
- Integration tests extend AbstractIntegrationTest (Testcontainers)
- Use fixture builders (PatientFixture, ClientFixture) for test data

## Database Migrations

This is a non-production application with non-production data. There is no need to create multiple migration files (V2, V3, etc.). All schema changes should be consolidated into the initial migration file:
- `backend/application/src/main/resources/db/migration/V1__initial_schema.sql`

When making database changes:
1. Modify V1__initial_schema.sql directly
2. Delete any numbered migrations (V2, V3, etc.) if they exist
3. Drop and recreate the database to apply changes

This keeps the schema clean and avoids migration complexity during development.
