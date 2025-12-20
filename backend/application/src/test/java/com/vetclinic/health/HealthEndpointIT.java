package com.vetclinic.health;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.http.ContentType;

/** Integration test for actuator health endpoints. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HealthEndpointIT {

    @LocalServerPort private int port;

    @Test
    void shouldReturnHealthStatus() {
        given().port(port)
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", notNullValue())
                .body("components", hasKey("database"))
                .body("components", hasKey("keycloak"));
    }

    @Test
    void shouldReturnLivenessProbe() {
        given().port(port)
                .when()
                .get("/actuator/health/liveness")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", is("UP"));
    }

    @Test
    void shouldReturnReadinessProbe() {
        given().port(port)
                .when()
                .get("/actuator/health/readiness")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", notNullValue());
    }

    @Test
    void shouldReturnApplicationInfo() {
        given().port(port)
                .when()
                .get("/actuator/info")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("app.name", equalTo("vet-clinic"))
                .body("app.description", equalTo("Veterinary Clinic Management System"));
    }

    @Test
    void shouldReturnDatabaseHealthDetails() {
        given().port(port)
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("components.database.status", is("UP"))
                .body("components.database.details.database", notNullValue())
                .body("components.database.details.validationQuery", equalTo("SELECT 1"));
    }

    @Test
    void shouldReturnKeycloakHealthDetails() {
        given().port(port)
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("components.keycloak", notNullValue())
                .body("components.keycloak.details.keycloakUrl", notNullValue())
                .body("components.keycloak.details.realm", notNullValue());
    }
}
