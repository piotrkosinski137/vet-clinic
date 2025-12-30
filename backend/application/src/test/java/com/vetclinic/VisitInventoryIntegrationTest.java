package com.vetclinic;

import static com.vetclinic.fixtures.ClientFixture.aClient;
import static com.vetclinic.fixtures.PatientFixture.aDog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;
import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.billing.domain.port.InventoryBatchRepository;
import com.vetclinic.billing.domain.port.InventoryTransactionRepository;
import com.vetclinic.billing.domain.port.PriceListRepository;
import com.vetclinic.common.tenant.TenantContext;
import com.vetclinic.config.TestSecurityConfig;

/**
 * Integration tests verifying that completing a visit correctly consumes inventory.
 *
 * <p>Tests the complete flow: Visit completion -> Event dispatched -> InventoryConsumptionListener
 * -> InventoryService.recordUsage -> Batch quantity deducted
 */
@Transactional
class VisitInventoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private PriceListRepository priceListRepository;

    @Autowired private InventoryBatchRepository batchRepository;

    @Autowired private InventoryTransactionRepository transactionRepository;

    private UUID patientId;
    private UUID clientId;

    @BeforeEach
    void setUp() throws Exception {
        // Set tenant context for direct repository operations
        TenantContext.setCurrentClinicId(TestSecurityConfig.TEST_CLINIC_ID);

        // Create a client via API
        var clientJson = aClient().withFullName("Test", "Client").toJson();
        var clientResult =
                mockMvc.perform(
                                post("/api/v1/clients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(clientJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        var clientResponse = objectMapper.readTree(clientResult.getResponse().getContentAsString());
        clientId = UUID.fromString(clientResponse.get("id").asText());

        // Create a patient via API
        var patientJson = aDog().withName("Test Patient").withOwnerId(clientId).toJson();
        var patientResult =
                mockMvc.perform(
                                post("/api/v1/patients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(patientJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        var patientResponse =
                objectMapper.readTree(patientResult.getResponse().getContentAsString());
        patientId = UUID.fromString(patientResponse.get("id").asText());
    }

    @Test
    void shouldDeductInventoryWhenVisitCompleted() throws Exception {
        // Set tenant context for repository operations (cleared after mockMvc calls)
        TenantContext.setCurrentClinicId(TestSecurityConfig.TEST_CLINIC_ID);

        // 1. Create a medication item with known stock
        var medication =
                PriceListItem.builder()
                        .name("Test Medication for Visit")
                        .category(ItemCategory.MEDICATION)
                        .unit("ml")
                        .costPrice(BigDecimal.valueOf(5.00))
                        .sellPrice(BigDecimal.valueOf(10.00))
                        .stockQuantity(BigDecimal.valueOf(100))
                        .build();
        medication = priceListRepository.save(medication);
        var itemId = medication.getId();

        // 2. Create a batch with 50 units
        var batch =
                InventoryBatch.builder()
                        .itemId(itemId)
                        .quantity(BigDecimal.valueOf(50))
                        .lotNumber("LOT-INTEGRATION-001")
                        .expirationDate(LocalDate.now().plusMonths(12))
                        .status(BatchStatus.COMPLETE)
                        .unitCost(BigDecimal.valueOf(5.00))
                        .build();
        batch = batchRepository.save(batch);
        var batchId = batch.getId();
        var initialQuantity = batch.getQuantity();

        // 3. Create a visit with this medication as usedMaterial
        var visitJson =
                """
                {
                    "patientId": "%s",
                    "clientId": "%s",
                    "visitDate": "%s",
                    "durationMinutes": 30,
                    "status": "IN_PROGRESS",
                    "reason": "Checkup",
                    "usedMaterials": [
                        {
                            "materialId": "%s",
                            "name": "Test Medication for Visit",
                            "quantity": 5.50,
                            "costPrice": 5.00,
                            "sellPrice": 10.00,
                            "unit": "ml"
                        }
                    ]
                }
                """
                        .formatted(patientId, clientId, LocalDateTime.now().plusHours(1), itemId);

        var createResult =
                mockMvc.perform(
                                post("/api/v1/visits")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(visitJson))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists())
                        .andReturn();

        var createdVisit = objectMapper.readTree(createResult.getResponse().getContentAsString());
        var visitId = createdVisit.get("id").asText();

        // 4. Complete the visit (triggers inventory consumption)
        var statusUpdateJson =
                """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(
                        put("/api/v1/visits/{id}/status", visitId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(statusUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 5. Verify batch quantity decreased
        var updatedBatch = batchRepository.findById(batchId).orElseThrow();
        assertThat(updatedBatch.getQuantity())
                .isEqualByComparingTo(initialQuantity.subtract(BigDecimal.valueOf(5.50)));

        // 6. Verify transaction was created
        var transactions = transactionRepository.findByItemId(itemId);
        assertThat(transactions).isNotEmpty();
        assertThat(transactions)
                .anyMatch(
                        tx ->
                                tx.getReferenceId() != null
                                        && tx.getReferenceType().equals("VISIT")
                                        && tx.getQuantity().compareTo(BigDecimal.valueOf(-5.50))
                                                == 0);
    }

    // Note: SERVICE item inventory behavior is covered by unit tests in:
    // - InventoryBatchServiceTest.shouldSkipDepletedBatchesInFIFOConsumption
    // - Unit tests for InventoryService.requiresInventoryTracking
    // Integration testing with @Transactional has isolation issues that make this test unreliable.

    // Note: Transaction rollback behavior is difficult to test with @Transactional.
    // The InsufficientStockException and transaction rollback are covered by unit tests in:
    // - InventoryConsumptionListenerTest.shouldThrowWhenConsumptionFails
    // - InventoryBatchServiceTest.shouldThrowInsufficientStockException

    @Test
    void shouldConsumeFractionalQuantitiesCorrectly() throws Exception {
        // Set tenant context for repository operations (cleared after mockMvc calls)
        TenantContext.setCurrentClinicId(TestSecurityConfig.TEST_CLINIC_ID);

        // 1. Create a medication item
        var medication =
                PriceListItem.builder()
                        .name("Fractional Medication")
                        .category(ItemCategory.MEDICATION)
                        .unit("ml")
                        .costPrice(BigDecimal.valueOf(2.50))
                        .sellPrice(BigDecimal.valueOf(5.00))
                        .stockQuantity(BigDecimal.valueOf(10.75))
                        .build();
        medication = priceListRepository.save(medication);
        var itemId = medication.getId();

        // 2. Create a batch with fractional quantity 10.75
        var batch =
                InventoryBatch.builder()
                        .itemId(itemId)
                        .quantity(BigDecimal.valueOf(10.75))
                        .lotNumber("LOT-FRAC-INT-001")
                        .expirationDate(LocalDate.now().plusMonths(6))
                        .status(BatchStatus.COMPLETE)
                        .unitCost(BigDecimal.valueOf(2.50))
                        .build();
        batch = batchRepository.save(batch);
        var batchId = batch.getId();

        // 3. Create and complete a visit consuming 3.25 units
        var visitJson =
                """
                {
                    "patientId": "%s",
                    "clientId": "%s",
                    "visitDate": "%s",
                    "durationMinutes": 30,
                    "status": "IN_PROGRESS",
                    "reason": "Treatment",
                    "usedMaterials": [
                        {
                            "materialId": "%s",
                            "name": "Fractional Medication",
                            "quantity": 3.25,
                            "costPrice": 2.50,
                            "sellPrice": 5.00,
                            "unit": "ml"
                        }
                    ]
                }
                """
                        .formatted(patientId, clientId, LocalDateTime.now().plusHours(4), itemId);

        var createResult =
                mockMvc.perform(
                                post("/api/v1/visits")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(visitJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        var createdVisit = objectMapper.readTree(createResult.getResponse().getContentAsString());
        var visitId = createdVisit.get("id").asText();

        // Complete the visit
        mockMvc.perform(
                        put("/api/v1/visits/{id}/status", visitId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isOk());

        // 4. Verify batch has exactly 7.50 remaining (10.75 - 3.25)
        var updatedBatch = batchRepository.findById(batchId).orElseThrow();
        assertThat(updatedBatch.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(7.50));
    }
}
