# Daily Quality Alignment Prompt for VetClinic Project

Copy and paste this prompt into Claude Code daily or after significant changes to maintain highest quality standards.

---

```
You are a senior software architect performing a comprehensive quality audit on the VetClinic project. Analyze the ENTIRE codebase systematically and fix ALL issues found. Use multiple parallel agents for thorough coverage.

## PHASE 1: SOLID PRINCIPLES AUDIT

### Single Responsibility Principle (SRP)
Scan for and fix:
- Services exceeding 300 lines (split into focused services)
- Classes with multiple unrelated responsibilities
- Methods doing more than one thing
- Controllers containing business logic
- Entities with behavior that belongs in services

Target areas to check:
- backend/**/domain/*Service.java
- backend/**/api/*Controller.java
- frontend/src/pages/*.tsx (components > 400 lines)
- frontend/src/hooks/*.ts

### Open/Closed Principle (OCP)
Scan for and fix:
- Hardcoded switch/case statements that should use strategy pattern
- Hardcoded configuration values that should be injectable
- Classes requiring modification for new variants

### Liskov Substitution Principle (LSP)
Scan for and fix:
- Subclasses that break parent contracts
- Overridden methods with different behavior expectations

### Interface Segregation Principle (ISP)
Scan for and fix:
- Fat interfaces that should be split
- Clients forced to depend on methods they don't use

### Dependency Inversion Principle (DIP)
Scan for and fix:
- Direct dependencies on concrete classes instead of interfaces
- Services directly instantiating dependencies
- Hard dependencies on external services without abstraction

## PHASE 2: DRY (Don't Repeat Yourself) AUDIT

Scan for and extract:
- Duplicate code blocks (>5 lines appearing 2+ times)
- Repeated patterns in exception handling
- Duplicate validation logic
- Repeated API call patterns in frontend
- Duplicate component structures
- Copy-paste DTOs or mappers

Create shared utilities for:
- Safe fetching patterns (null-safe service calls)
- Change detection patterns
- Query string building
- Error handling wrappers
- Form validation patterns

## PHASE 3: MAGIC VALUES ELIMINATION

### Backend - Find and extract to constants:
- Hardcoded numbers (durations, limits, thresholds)
- Hardcoded strings (status values, error messages)
- Regex patterns without named constants
- Configuration values in code

Check files:
- All *Service.java files
- All *Seeder.java files
- All *Controller.java files

### Frontend - Find and extract to constants:
- Timeout values (move to src/constants/timings.ts)
- Debounce delays
- Retry counts
- API endpoints
- Color hex codes not in theme
- Priority order numbers
- i18n keys (consider src/constants/i18nKeys.ts)

Check files:
- frontend/src/api/client.ts
- frontend/src/hooks/*.ts
- frontend/src/pages/*.tsx
- frontend/src/components/**/*.tsx

## PHASE 4: METHOD QUALITY

### Backend methods must be:
- Under 20 lines (extract helper methods if longer)
- Maximum 4 parameters (use parameter objects if more)
- Single level of abstraction
- Descriptively named (no comments needed)

### Frontend functions must be:
- Under 30 lines for components
- Under 15 lines for utility functions
- Wrapped in useCallback when passed as props
- Properly memoized with useMemo for expensive computations

### Check for:
- Nested conditionals (max 2 levels - extract to methods)
- Long parameter lists (create typed objects)
- Methods with boolean parameters (split into two methods)
- God methods doing everything

## PHASE 5: ERROR HANDLING AUDIT

### Backend:
- No generic catch(Exception e) - catch specific exceptions
- All exceptions must have meaningful messages
- Create domain-specific exceptions where missing
- Ensure proper error propagation
- Log stack traces for unexpected errors
- Use @ControllerAdvice consistently

### Frontend:
- All API calls wrapped in try-catch
- Loading states for async operations
- Error states displayed to users
- Error boundaries at appropriate levels
- Toast/notification for user-facing errors
- No silent failures (log if not showing to user)

## PHASE 6: EDGE CASE HANDLING

### Null/Undefined Safety:
- All nullable parameters validated
- Optional.orElse() patterns for defaults
- Frontend optional chaining (?.) used appropriately
- No truthy checks that fail on 0 or empty string

### Empty Collections:
- Empty list handling (no IndexOutOfBounds)
- Pagination for unbounded queries (MAX 100 results default)
- Stream operations handle empty case

### Boundary Conditions:
- Date boundaries (start of day, end of day)
- Numeric limits (min/max validation)
- String length limits
- Future date validation where applicable
- Past date validation where applicable

### Concurrent Access:
- Optimistic locking for updates (@Version)
- Race condition handling in waiting room
- Idempotent API endpoints

## PHASE 7: TYPE SAFETY

### Backend:
- Use var for local variables (project guideline)
- Use records for immutable DTOs
- Generic types properly constrained
- No raw types

### Frontend:
- No 'any' types - use proper interfaces
- API responses fully typed
- Props interfaces for all components
- No type assertions without validation
- Runtime validation for API responses (consider zod)

## PHASE 8: SERVICE LAYER ARCHITECTURE

### Extract helper classes where services exceed 300 lines:
- *ValidationService for validation logic
- *Processor for complex processing
- *Calculator for calculation logic
- *Mapper for complex transformations

### Ensure proper layering:
- Controllers: Only HTTP concerns, delegation
- Services: Business logic only
- Repositories: Data access only
- Domain: Rich domain models where appropriate

## PHASE 9: TESTING GAPS

### Verify tests exist for:
- Every public service method
- Edge cases (null, empty, boundary values)
- Error scenarios (exceptions thrown)
- Event publishing verification
- Validation rule enforcement

### Test quality checks:
- No implementation testing (verify behavior, not mocks)
- Deterministic tests (inject Clock for dates)
- No flaky tests (no Thread.sleep, proper async handling)
- Use test fixtures from /fixtures package

### Missing test coverage to add:
- VeterinarianService unit tests
- InventoryService unit tests
- AuditService unit tests
- PaymentService edge cases
- Boundary value tests for all validators

## PHASE 10: SECURITY AUDIT

### Check for:
- SQL injection (parameterized queries only)
- XSS prevention (proper escaping)
- CSRF tokens where needed
- Authorization on all endpoints (@PreAuthorize)
- Input validation on all DTOs
- File upload validation (type, size)
- Sensitive data not logged
- Tokens stored securely (httpOnly cookies preferred)

## PHASE 11: PERFORMANCE AUDIT

### Check for:
- N+1 query patterns (use @EntityGraph)
- Missing pagination on list endpoints
- Unbounded findAll() calls
- Missing database indexes for search fields
- Caching opportunities for static data
- Frontend bundle size optimization
- Unnecessary re-renders (React.memo, useMemo)

## PHASE 12: MODERN PATTERNS

### Backend must use:
- Stream API for collection processing
- Optional for nullable returns
- Records for immutable data
- Method references where cleaner
- try-with-resources for closeable resources
- CompletableFuture for async operations

### Frontend must use:
- Custom hooks for reusable logic
- useCallback for function props
- useMemo for expensive computations
- Proper dependency arrays
- Suspense/lazy for code splitting where beneficial

## PHASE 13: CODE ORGANIZATION

### File structure:
- One class per file
- Logical package/folder structure
- Constants in dedicated files
- Types/interfaces exported from index files
- No circular dependencies

### Naming conventions:
- Descriptive variable names (no abbreviations)
- Consistent naming patterns
- Boolean variables start with is/has/can
- Collections are plural
- Methods describe action

## PHASE 14: DOCUMENTATION

### Required:
- Complex algorithms documented
- Public APIs have clear contracts
- Non-obvious business rules explained
- Architecture decisions recorded
- API endpoints in postman-requests.json

### Not required (avoid over-documentation):
- Obvious getter/setter docs
- Self-explanatory method docs
- Comments that duplicate code

## EXECUTION INSTRUCTIONS

1. Use multiple parallel Explore agents to scan different areas simultaneously
2. Create TodoWrite list of ALL issues found
3. Fix issues in order of severity (Critical > High > Medium > Low)
4. Run tests after each significant change
5. Run ./mvnw spotless:apply before finishing
6. Do TWO full iterations - second iteration catches issues created by first fixes
7. Verify no regressions introduced

## QUALITY GATES (Must Pass)

- [ ] All services under 400 lines
- [ ] All methods under 25 lines
- [ ] No magic numbers in code
- [ ] No duplicate code blocks
- [ ] All public methods have tests
- [ ] No 'any' types in TypeScript
- [ ] All API calls have error handling
- [ ] All endpoints have authorization
- [ ] No N+1 queries
- [ ] All lists paginated
- [ ] Build passes with no warnings
- [ ] All tests pass
```
