package com.vetclinic;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vetclinic.config.TestSecurityConfig;
import com.vetclinic.veterinarian.api.dto.VeterinarianDayOffRequest;
import com.vetclinic.veterinarian.api.dto.VeterinarianScheduleRequest;
import com.vetclinic.veterinarian.api.dto.WeeklyScheduleRequest;
import com.vetclinic.veterinarian.domain.model.DayOffType;
import com.vetclinic.veterinarian.domain.model.Veterinarian;
import com.vetclinic.veterinarian.domain.port.VeterinarianRepository;

/**
 * Integration tests for Veterinarian Schedule API. Tests schedule management, days off management,
 * and availability checking.
 */
@Transactional
@DisplayName("Veterinarian Schedule API")
class VeterinarianScheduleApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private VeterinarianRepository veterinarianRepository;

    private UUID veterinarianId;

    @BeforeEach
    void setUp() {
        // Use the default test clinic ID from TestSecurityConfig
        var clinicId = TestSecurityConfig.TEST_CLINIC_ID;

        // Create a veterinarian for this test
        var vet =
                Veterinarian.builder()
                        .firstName("Jan")
                        .lastName("Kowalski")
                        .email("jan.schedule.test." + UUID.randomUUID() + "@vetclinic.pl")
                        .specialization("General")
                        .active(true)
                        .build();
        vet.setClinicId(clinicId);
        vet = veterinarianRepository.save(vet);
        veterinarianId = vet.getId();
    }

    @Nested
    @DisplayName("Weekly Schedule Endpoints")
    class WeeklyScheduleEndpoints {

        @Test
        @DisplayName("GET /veterinarians/{id}/schedule - should return empty schedule initially")
        void shouldReturnEmptyScheduleInitially() throws Exception {
            mockMvc.perform(get("/api/v1/veterinarians/{id}/schedule", veterinarianId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("PUT /veterinarians/{id}/schedule - should update weekly schedule")
        void shouldUpdateWeeklySchedule() throws Exception {
            var request =
                    new WeeklyScheduleRequest(
                            List.of(
                                    new VeterinarianScheduleRequest(
                                            DayOfWeek.MONDAY,
                                            LocalTime.of(8, 0),
                                            LocalTime.of(16, 0),
                                            true),
                                    new VeterinarianScheduleRequest(
                                            DayOfWeek.TUESDAY,
                                            LocalTime.of(9, 0),
                                            LocalTime.of(17, 0),
                                            true),
                                    new VeterinarianScheduleRequest(
                                            DayOfWeek.WEDNESDAY,
                                            LocalTime.of(8, 0),
                                            LocalTime.of(14, 0),
                                            true),
                                    new VeterinarianScheduleRequest(
                                            DayOfWeek.THURSDAY,
                                            LocalTime.of(10, 0),
                                            LocalTime.of(18, 0),
                                            true),
                                    new VeterinarianScheduleRequest(
                                            DayOfWeek.FRIDAY,
                                            LocalTime.of(8, 0),
                                            LocalTime.of(12, 0),
                                            true),
                                    new VeterinarianScheduleRequest(
                                            DayOfWeek.SATURDAY, null, null, false),
                                    new VeterinarianScheduleRequest(
                                            DayOfWeek.SUNDAY, null, null, false)));

            mockMvc.perform(
                            put("/api/v1/veterinarians/{id}/schedule", veterinarianId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(7))
                    .andExpect(jsonPath("$[?(@.dayOfWeek=='MONDAY')].workingDay").value(true))
                    .andExpect(jsonPath("$[?(@.dayOfWeek=='SATURDAY')].workingDay").value(false));
        }

        @Test
        @DisplayName("GET /veterinarians/{id}/schedule - should retrieve updated schedule")
        void shouldRetrieveUpdatedSchedule() throws Exception {
            // First update the schedule with a full week (validation requires 7 days)
            var request = createFullWeekSchedule();

            mockMvc.perform(
                            put("/api/v1/veterinarians/{id}/schedule", veterinarianId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Then retrieve it
            mockMvc.perform(get("/api/v1/veterinarians/{id}/schedule", veterinarianId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.dayOfWeek=='MONDAY')].startTime").value("09:00:00"))
                    .andExpect(jsonPath("$[?(@.dayOfWeek=='MONDAY')].endTime").value("17:00:00"))
                    .andExpect(jsonPath("$[?(@.dayOfWeek=='MONDAY')].workingDay").value(true));
        }

        @Test
        @DisplayName("PUT /veterinarians/{id}/schedule - should return 404 for non-existent vet")
        void shouldReturn404ForNonExistentVet() throws Exception {
            var nonExistentId = UUID.randomUUID();
            // Must provide valid 7-day schedule to pass validation before checking vet existence
            var request = createFullWeekSchedule();

            mockMvc.perform(
                            put("/api/v1/veterinarians/{id}/schedule", nonExistentId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        private WeeklyScheduleRequest createFullWeekSchedule() {
            return new WeeklyScheduleRequest(
                    List.of(
                            new VeterinarianScheduleRequest(
                                    DayOfWeek.MONDAY,
                                    LocalTime.of(9, 0),
                                    LocalTime.of(17, 0),
                                    true),
                            new VeterinarianScheduleRequest(
                                    DayOfWeek.TUESDAY,
                                    LocalTime.of(9, 0),
                                    LocalTime.of(17, 0),
                                    true),
                            new VeterinarianScheduleRequest(
                                    DayOfWeek.WEDNESDAY,
                                    LocalTime.of(9, 0),
                                    LocalTime.of(17, 0),
                                    true),
                            new VeterinarianScheduleRequest(
                                    DayOfWeek.THURSDAY,
                                    LocalTime.of(9, 0),
                                    LocalTime.of(17, 0),
                                    true),
                            new VeterinarianScheduleRequest(
                                    DayOfWeek.FRIDAY,
                                    LocalTime.of(9, 0),
                                    LocalTime.of(17, 0),
                                    true),
                            new VeterinarianScheduleRequest(DayOfWeek.SATURDAY, null, null, false),
                            new VeterinarianScheduleRequest(DayOfWeek.SUNDAY, null, null, false)));
        }
    }

    @Nested
    @DisplayName("Days Off Endpoints")
    class DaysOffEndpoints {

        @Test
        @DisplayName("GET /veterinarians/{id}/days-off - should return empty list initially")
        void shouldReturnEmptyDaysOffInitially() throws Exception {
            mockMvc.perform(get("/api/v1/veterinarians/{id}/days-off", veterinarianId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("POST /veterinarians/{id}/days-off - should add day off")
        void shouldAddDayOff() throws Exception {
            var request =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 7, 1),
                            LocalDate.of(2024, 7, 14),
                            DayOffType.VACATION,
                            "Summer vacation");

            mockMvc.perform(
                            post("/api/v1/veterinarians/{id}/days-off", veterinarianId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.veterinarianId").value(veterinarianId.toString()))
                    .andExpect(jsonPath("$.startDate").value("2024-07-01"))
                    .andExpect(jsonPath("$.endDate").value("2024-07-14"))
                    .andExpect(jsonPath("$.type").value("VACATION"))
                    .andExpect(jsonPath("$.description").value("Summer vacation"))
                    .andExpect(jsonPath("$.approved").value(false));
        }

        @Test
        @DisplayName("POST /veterinarians/{id}/days-off - should reject invalid date range")
        void shouldRejectInvalidDateRange() throws Exception {
            var request =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 7, 14),
                            LocalDate.of(2024, 7, 1), // End before start
                            DayOffType.VACATION,
                            null);

            mockMvc.perform(
                            post("/api/v1/veterinarians/{id}/days-off", veterinarianId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /veterinarians/{id}/days-off/{dayOffId} - should update day off")
        void shouldUpdateDayOff() throws Exception {
            // Create a day off first
            var createRequest =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 8, 1),
                            LocalDate.of(2024, 8, 5),
                            DayOffType.PERSONAL,
                            "Original");

            MvcResult createResult =
                    mockMvc.perform(
                                    post("/api/v1/veterinarians/{id}/days-off", veterinarianId)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(createRequest)))
                            .andExpect(status().isCreated())
                            .andReturn();

            JsonNode responseJson =
                    objectMapper.readTree(createResult.getResponse().getContentAsString());
            String dayOffId = responseJson.get("id").asText();

            // Update the day off
            var updateRequest =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 8, 1),
                            LocalDate.of(2024, 8, 10),
                            DayOffType.SICK_LEAVE,
                            "Extended sick leave");

            mockMvc.perform(
                            put(
                                            "/api/v1/veterinarians/{id}/days-off/{dayOffId}",
                                            veterinarianId,
                                            dayOffId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.endDate").value("2024-08-10"))
                    .andExpect(jsonPath("$.type").value("SICK_LEAVE"))
                    .andExpect(jsonPath("$.description").value("Extended sick leave"));
        }

        @Test
        @DisplayName("DELETE /veterinarians/{id}/days-off/{dayOffId} - should delete day off")
        void shouldDeleteDayOff() throws Exception {
            // Create a day off first
            var request =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 9, 1),
                            LocalDate.of(2024, 9, 3),
                            DayOffType.OTHER,
                            null);

            MvcResult createResult =
                    mockMvc.perform(
                                    post("/api/v1/veterinarians/{id}/days-off", veterinarianId)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isCreated())
                            .andReturn();

            JsonNode responseJson =
                    objectMapper.readTree(createResult.getResponse().getContentAsString());
            String dayOffId = responseJson.get("id").asText();

            // Delete it
            mockMvc.perform(
                            delete(
                                    "/api/v1/veterinarians/{id}/days-off/{dayOffId}",
                                    veterinarianId,
                                    dayOffId))
                    .andExpect(status().isNoContent());

            // Verify it's gone
            mockMvc.perform(get("/api/v1/veterinarians/{id}/days-off", veterinarianId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName(
                "PATCH /veterinarians/{id}/days-off/{dayOffId}/approve - should approve day off")
        void shouldApproveDayOff() throws Exception {
            // Create a day off first
            var request =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 10, 1),
                            LocalDate.of(2024, 10, 5),
                            DayOffType.VACATION,
                            null);

            MvcResult createResult =
                    mockMvc.perform(
                                    post("/api/v1/veterinarians/{id}/days-off", veterinarianId)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isCreated())
                            .andReturn();

            JsonNode responseJson =
                    objectMapper.readTree(createResult.getResponse().getContentAsString());
            String dayOffId = responseJson.get("id").asText();

            // Approve it
            mockMvc.perform(
                            patch(
                                            "/api/v1/veterinarians/{id}/days-off/{dayOffId}/approve",
                                            veterinarianId,
                                            dayOffId)
                                    .param("approved", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.approved").value(true));
        }
    }

    @Nested
    @DisplayName("Availability Endpoints")
    class AvailabilityEndpoints {

        @Test
        @DisplayName("GET /veterinarians/{id}/availability - should return default availability")
        void shouldReturnDefaultAvailability() throws Exception {
            var today = LocalDate.now();

            mockMvc.perform(
                            get("/api/v1/veterinarians/{id}/availability", veterinarianId)
                                    .param("date", today.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.veterinarianId").value(veterinarianId.toString()))
                    .andExpect(jsonPath("$.date").value(today.toString()))
                    .andExpect(jsonPath("$.isDayOff").value(false));
        }

        @Test
        @DisplayName("GET /veterinarians/{id}/availability - should show day off status")
        void shouldShowDayOffStatus() throws Exception {
            var dayOffDate = LocalDate.of(2024, 12, 25);

            // Add a day off
            var request =
                    new VeterinarianDayOffRequest(
                            dayOffDate, dayOffDate, DayOffType.VACATION, "Christmas");

            mockMvc.perform(
                            post("/api/v1/veterinarians/{id}/days-off", veterinarianId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Check availability
            mockMvc.perform(
                            get("/api/v1/veterinarians/{id}/availability", veterinarianId)
                                    .param("date", dayOffDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isDayOff").value(true))
                    .andExpect(jsonPath("$.dayOffType").value("VACATION"))
                    .andExpect(jsonPath("$.workingDay").value(false));
        }

        @Test
        @DisplayName("GET /veterinarians/availability - should return all vets availability")
        void shouldReturnAllVetsAvailability() throws Exception {
            var today = LocalDate.now();

            mockMvc.perform(
                            get("/api/v1/veterinarians/availability")
                                    .param("date", today.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }
}
