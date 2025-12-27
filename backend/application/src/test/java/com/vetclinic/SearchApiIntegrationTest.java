package com.vetclinic;

import static com.vetclinic.fixtures.ClientFixture.aClient;
import static com.vetclinic.fixtures.PatientFixture.aCat;
import static com.vetclinic.fixtures.PatientFixture.aDog;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
class SearchApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Nested
    class PatientSearch {
        @Test
        void shouldSearchPatientsByName() throws Exception {
            // Create patients
            createPatient(aDog().withName("Buddy").toJson());
            createPatient(aCat().withName("Buddy Jr").toJson());
            createPatient(aDog().withName("Max").toJson());

            // Search by name
            mockMvc.perform(get("/api/v1/patients").param("name", "Buddy"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void shouldSearchPatientsBySpecies() throws Exception {
            // Create patients
            createPatient(aDog().withName("Buddy").toJson());
            createPatient(aDog().withName("Max").toJson());
            createPatient(aCat().withName("Whiskers").toJson());

            // Search by species
            mockMvc.perform(get("/api/v1/patients").param("species", "DOG"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void shouldReturnAllPatientsWhenNoSearchCriteria() throws Exception {
            // Create patients
            createPatient(aDog().withName("Buddy").toJson());
            createPatient(aCat().withName("Whiskers").toJson());

            // Get all patients
            mockMvc.perform(get("/api/v1/patients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        private String createPatient(String json) throws Exception {
            MvcResult result =
                    mockMvc.perform(
                                    post("/api/v1/patients")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andExpect(status().isCreated())
                            .andReturn();

            JsonNode responseJson =
                    objectMapper.readTree(result.getResponse().getContentAsString());
            return responseJson.get("id").asText();
        }
    }

    @Nested
    class ClientSearch {
        @Test
        void shouldSearchClientsByFirstName() throws Exception {
            // Create clients
            createClient(aClient().withFirstName("John").withLastName("Doe").toJson());
            createClient(aClient().withFirstName("Johnny").withLastName("Smith").toJson());
            createClient(aClient().withFirstName("Jane").withLastName("Doe").toJson());

            // Search by first name
            mockMvc.perform(get("/api/v1/clients").param("firstName", "John"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void shouldSearchClientsByCity() throws Exception {
            // Create clients
            createClient(aClient().withCity("Warsaw").toJson());
            createClient(aClient().withCity("Krakow").toJson());

            // Search by city
            mockMvc.perform(get("/api/v1/clients").param("city", "Warsaw"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void shouldReturnAllClientsWhenNoSearchCriteria() throws Exception {
            // Create clients
            createClient(aClient().withFirstName("John").toJson());
            createClient(aClient().withFirstName("Jane").toJson());

            // Get all clients
            mockMvc.perform(get("/api/v1/clients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        private void createClient(String json) throws Exception {
            mockMvc.perform(
                            post("/api/v1/clients")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated());
        }
    }
}
