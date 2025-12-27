package com.vetclinic;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vetclinic.config.TestSecurityConfig;

/**
 * Base class for integration tests that provides a shared PostgreSQL Testcontainer. This ensures
 * that all integration tests run against a real PostgreSQL database, making them more reliable and
 * closer to production.
 *
 * <p>Uses the Singleton Container Pattern to share a single PostgreSQL container across all test
 * classes. The container is started once and kept running for the entire test suite.
 *
 * <p>Features: - Uses Testcontainers to provide a real PostgreSQL database - Automatically
 * configures Spring datasource properties - Disables OAuth2/Keycloak authentication via
 * TestSecurityConfig - Configures MockMvc for API testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL container shared across all tests using the Singleton Container Pattern. Started
     * once in a static block and kept running for all test classes.
     */
    protected static final PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer =
                new PostgreSQLContainer<>("postgres:16-alpine")
                        .withDatabaseName("testdb")
                        .withUsername("test")
                        .withPassword("test");
        postgresContainer.start();
    }

    /**
     * Dynamically configures Spring Boot properties to connect to the Testcontainers PostgreSQL
     * instance. This overrides any properties defined in application-test.yml.
     */
    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
