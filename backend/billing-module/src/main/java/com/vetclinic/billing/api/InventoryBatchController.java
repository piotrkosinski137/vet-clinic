package com.vetclinic.billing.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_INVENTORY;
import static com.vetclinic.common.security.Roles.CAN_VIEW_FINANCIALS;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.billing.api.dto.CompleteBatchRequest;
import com.vetclinic.billing.api.dto.DisposeBatchRequest;
import com.vetclinic.billing.api.dto.InventoryBatchResponse;
import com.vetclinic.billing.domain.InventoryBatchService;
import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/inventory/batches")
@RequiredArgsConstructor
@Slf4j
public class InventoryBatchController {

    private final InventoryBatchService batchService;

    /**
     * Get all batches, optionally filtered by status or itemId.
     *
     * @param status Optional status filter (PENDING, COMPLETE, DEPLETED)
     * @param itemId Optional item filter
     * @return List of batches
     */
    @GetMapping
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<List<InventoryBatchResponse>> getBatches(
            @RequestParam(required = false) BatchStatus status,
            @RequestParam(required = false) UUID itemId) {
        List<InventoryBatch> batches;

        if (itemId != null) {
            batches = batchService.getBatchesByItemId(itemId);
        } else if (status != null) {
            batches = batchService.getBatchesByStatus(status);
        } else {
            batches = batchService.getBatches();
        }

        var responses = batches.stream().map(InventoryBatchResponse::from).toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get batch by ID.
     *
     * @param id Batch ID
     * @return Batch details
     */
    @GetMapping("/{id}")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<InventoryBatchResponse> getBatchById(@PathVariable UUID id) {
        var batch = batchService.getBatchById(id);
        return ResponseEntity.ok(InventoryBatchResponse.from(batch));
    }

    /**
     * Get batches expiring within the given number of days.
     *
     * @param days Number of days (default 30)
     * @return List of expiring batches
     */
    @GetMapping("/expiring")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<List<InventoryBatchResponse>> getExpiringBatches(
            @RequestParam(defaultValue = "30") int days) {
        log.info("Getting batches expiring within {} days", days);

        var batches = batchService.getExpiringSoon(days);
        var responses = batches.stream().map(InventoryBatchResponse::from).toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get count of pending batches (for badge display).
     *
     * @return Count of pending batches
     */
    @GetMapping("/pending/count")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<Long> getPendingBatchCount() {
        var count = batchService.countPendingBatches();
        return ResponseEntity.ok(count);
    }

    /**
     * Complete a pending batch by setting LOT number and expiration date.
     *
     * @param id Batch ID
     * @param request LOT number and expiration date
     * @return Updated batch
     */
    @PatchMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_INVENTORY)
    public ResponseEntity<InventoryBatchResponse> completeBatch(
            @PathVariable UUID id, @Valid @RequestBody CompleteBatchRequest request) {
        log.info(
                "Completing batch {}: LOT={}, expires={}",
                id,
                request.lotNumber(),
                request.expirationDate());

        var batch =
                batchService.completePendingBatch(
                        id, request.lotNumber(), request.expirationDate());
        return ResponseEntity.ok(InventoryBatchResponse.from(batch));
    }

    /**
     * Dispose a batch (e.g., expired stock).
     *
     * @param id Batch ID
     * @param request Quantity and reason for disposal
     * @return Updated batch
     */
    @PostMapping("/{id}/dispose")
    @PreAuthorize(CAN_MANAGE_INVENTORY)
    public ResponseEntity<InventoryBatchResponse> disposeBatch(
            @PathVariable UUID id, @Valid @RequestBody DisposeBatchRequest request) {
        log.info("Disposing {} units from batch {}: {}", request.quantity(), id, request.reason());

        var batch = batchService.disposeBatch(id, request.quantity(), request.reason());
        return ResponseEntity.ok(InventoryBatchResponse.from(batch));
    }
}
