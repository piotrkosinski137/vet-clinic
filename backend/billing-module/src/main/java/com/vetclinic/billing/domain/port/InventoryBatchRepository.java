package com.vetclinic.billing.domain.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;

public interface InventoryBatchRepository {

    InventoryBatch save(InventoryBatch batch);

    Optional<InventoryBatch> findById(UUID id);

    List<InventoryBatch> findByClinicId(UUID clinicId);

    List<InventoryBatch> findByItemId(UUID itemId);

    /**
     * Find batches for FIFO consumption: only COMPLETE batches, ordered by expiration date
     * (earliest first, nulls last).
     */
    List<InventoryBatch> findForFifoConsumption(UUID itemId);

    List<InventoryBatch> findByClinicIdAndStatus(UUID clinicId, BatchStatus status);

    List<InventoryBatch> findByItemIdAndStatus(UUID itemId, BatchStatus status);

    /** Find batches expiring before the given date with quantity > 0. */
    List<InventoryBatch> findExpiringBefore(UUID clinicId, LocalDate date);

    /** Find batches expiring within the given number of days. */
    List<InventoryBatch> findExpiringSoon(UUID clinicId, int days);

    /** Count pending batches (for badge display). */
    long countByClinicIdAndStatus(UUID clinicId, BatchStatus status);

    /** Sum quantity of all batches for an item (for stock recalculation). */
    BigDecimal sumQuantityByItemId(UUID itemId);

    void delete(InventoryBatch batch);

    void deleteById(UUID id);
}
