package com.vetclinic;

import static com.vetclinic.fixtures.PatientFixture.aBird;
import static com.vetclinic.fixtures.PatientFixture.aCat;
import static com.vetclinic.fixtures.PatientFixture.aDog;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vetclinic.config.TestSecurityConfig;

/**
 * Integration tests for Patient API. Uses TestSecurityConfig to disable OAuth2/Keycloak
 * authentication, allowing tests to focus on business logic.
 *
 * <p>Each test runs in a transaction that is rolled back after completion, ensuring test isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class PatientApiIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndRetrievePatient() throws Exception {
        // Create a patient using fixture
        String patientJson = aDog().withName("Buddy").withWeight(30.5).toJson();

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/v1/patients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(patientJson))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("Buddy"))
                        .andExpect(jsonPath("$.species").value("DOG"))
                        .andExpect(jsonPath("$.weight").value(30.5))
                        .andExpect(jsonPath("$.id").exists())
                        .andReturn();

        // Extract created patient ID
        JsonNode responseJson =
                objectMapper.readTree(createResult.getResponse().getContentAsString());
        String patientId = responseJson.get("id").asText();

        // Retrieve the patient by ID
        mockMvc.perform(get("/api/v1/patients/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId))
                .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    void shouldReturnEmptyListWhenNoPatients() throws Exception {
        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnBadRequestForInvalidPatient() throws Exception {
        // Missing required fields (name and species)
        String invalidPatientJson = "{\"breed\": \"Golden Retriever\"}";

        mockMvc.perform(
                        post("/api/v1/patients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidPatientJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatePatient() throws Exception {
        // First create a patient using fixture
        String patientJson = aCat().withName("Max").toJson();

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/v1/patients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(patientJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode responseJson =
                objectMapper.readTree(createResult.getResponse().getContentAsString());
        String patientId = responseJson.get("id").asText();

        // Update the patient
        String updateJson = aCat().withName("Maximus").withWeight(5.5).toJson();

        mockMvc.perform(
                        put("/api/v1/patients/{id}", patientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maximus"))
                .andExpect(jsonPath("$.weight").value(5.5));
    }

    @Test
    void shouldDeletePatient() throws Exception {
        // Create a patient using fixture
        String patientJson = aBird().withName("ToDelete").toJson();

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/v1/patients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(patientJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode responseJson =
                objectMapper.readTree(createResult.getResponse().getContentAsString());
        String patientId = responseJson.get("id").asText();

        // Delete the patient
        mockMvc.perform(delete("/api/v1/patients/{id}", patientId))
                .andExpect(status().isNoContent());

        // Verify patient is gone
        mockMvc.perform(get("/api/v1/patients/{id}", patientId)).andExpect(status().isNotFound());
    }
}
