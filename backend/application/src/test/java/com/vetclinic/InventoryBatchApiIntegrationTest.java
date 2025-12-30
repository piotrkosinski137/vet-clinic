package com.vetclinic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
class InventoryBatchApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldGetAllBatches() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetBatchesByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/batches").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetBatchesByCompletedStatus() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/batches").param("status", "COMPLETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetPendingBatchCount() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/batches/pending/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void shouldGetExpiringBatches() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/batches/expiring").param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnNotFoundForNonExistentBatch() throws Exception {
        var randomId = UUID.randomUUID();
        mockMvc.perform(
                        patch("/api/v1/inventory/batches/{id}", randomId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                    {
                        "lotNumber": "LOT-TEST-001",
                        "expirationDate": "2025-12-31"
                    }
                    """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDisposingNonExistentBatch() throws Exception {
        var randomId = UUID.randomUUID();
        mockMvc.perform(
                        post("/api/v1/inventory/batches/{id}/dispose", randomId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                    {
                        "quantity": 10,
                        "reason": "Expired stock"
                    }
                    """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateDisposeBatchRequest() throws Exception {
        var randomId = UUID.randomUUID();
        mockMvc.perform(
                        post("/api/v1/inventory/batches/{id}/dispose", randomId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                    {
                        "quantity": 0,
                        "reason": ""
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateCompleteBatchRequest() throws Exception {
        var randomId = UUID.randomUUID();
        mockMvc.perform(
                        patch("/api/v1/inventory/batches/{id}", randomId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                    {
                        "lotNumber": ""
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDisposeExistingBatchSuccessfully() throws Exception {
        // First GET /api/v1/inventory/batches?status=COMPLETE to get a batch
        var getBatchesResult =
                mockMvc.perform(get("/api/v1/inventory/batches").param("status", "COMPLETE"))
                        .andExpect(status().isOk())
                        .andReturn();

        var batchesJson = getBatchesResult.getResponse().getContentAsString();
        var batches = objectMapper.readTree(batchesJson);

        // Find first batch with quantity > 0
        JsonNode batchToDispose = null;
        for (var batch : batches) {
            if (batch.get("quantity").asInt() > 0) {
                batchToDispose = batch;
                break;
            }
        }

        // Skip test if no complete batch with stock is available
        if (batchToDispose == null) {
            return;
        }

        var batchId = batchToDispose.get("id").asText();
        var originalQuantity = batchToDispose.get("quantity").asInt();

        // POST dispose request with quantity=1 and reason="Integration test"
        var disposeResult =
                mockMvc.perform(
                                post("/api/v1/inventory/batches/{id}/dispose", batchId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                            {
                                "quantity": 1,
                                "reason": "Integration test"
                            }
                            """))
                        .andExpect(status().isOk())
                        .andReturn();

        // Verify quantity decreased by 1
        var disposedBatchJson = disposeResult.getResponse().getContentAsString();
        var disposedBatch = objectMapper.readTree(disposedBatchJson);
        var newQuantity = disposedBatch.get("quantity").asInt();

        assertThat(newQuantity).isEqualTo(originalQuantity - 1);
    }
}
