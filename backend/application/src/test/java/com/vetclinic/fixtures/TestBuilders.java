package com.vetclinic.fixtures;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Central registry of test builders for creating test data.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * // Create a patient entity
 * Patient patient = TestBuilders.patient().withName("Max").build();
 *
 * // Create a patient request DTO
 * PatientRequest request = TestBuilders.patient().withName("Max").buildRequest();
 *
 * // Create JSON for API testing
 * String json = TestBuilders.patient().withName("Max").toJson();
 *
 * // Convenience factory methods
 * Patient cat = TestBuilders.cat().withName("Whiskers").build();
 * Client client = TestBuilders.client().withFullName("John", "Doe").build();
 * }</pre>
 */
public final class TestBuilders {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private TestBuilders() {
        // Utility class
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Returns the shared ObjectMapper for JSON serialization. Configured with JavaTimeModule for
     * proper date/time handling.
     */
    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Serialize any object to JSON string.
     *
     * @param object the object to serialize
     * @return JSON string representation
     * @throws IllegalStateException if serialization fails
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize object to JSON", e);
        }
    }

    // ==================== Patient Builders ====================

    /** Creates a patient builder with default values. */
    public static PatientFixture patient() {
        return PatientFixture.aPatient();
    }

    /** Creates a dog patient builder. */
    public static PatientFixture dog() {
        return PatientFixture.aDog();
    }

    /** Creates a cat patient builder. */
    public static PatientFixture cat() {
        return PatientFixture.aCat();
    }

    /** Creates a bird patient builder. */
    public static PatientFixture bird() {
        return PatientFixture.aBird();
    }

    // ==================== Client Builders ====================

    /** Creates a client builder with default values. */
    public static ClientFixture client() {
        return ClientFixture.aClient();
    }

    /** Creates a client builder with notes indicating pet ownership. */
    public static ClientFixture clientWithPets() {
        return ClientFixture.aClientWithPets();
    }
}
