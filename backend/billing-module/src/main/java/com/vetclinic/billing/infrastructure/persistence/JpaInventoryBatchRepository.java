package com.vetclinic.billing.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;

public interface JpaInventoryBatchRepository extends JpaRepository<InventoryBatch, UUID> {

    @Query("SELECT b FROM InventoryBatch b LEFT JOIN FETCH b.item WHERE b.clinicId = :clinicId")
    List<InventoryBatch> findByClinicId(@Param("clinicId") UUID clinicId);

    @Query("SELECT b FROM InventoryBatch b LEFT JOIN FETCH b.item WHERE b.itemId = :itemId")
    List<InventoryBatch> findByItemId(@Param("itemId") UUID itemId);

    @Query(
            "SELECT b FROM InventoryBatch b LEFT JOIN FETCH b.item WHERE b.clinicId = :clinicId AND b.status = :status")
    List<InventoryBatch> findByClinicIdAndStatus(
            @Param("clinicId") UUID clinicId, @Param("status") BatchStatus status);

    @Query(
            "SELECT b FROM InventoryBatch b LEFT JOIN FETCH b.item WHERE b.itemId = :itemId AND b.status = :status")
    List<InventoryBatch> findByItemIdAndStatus(
            @Param("itemId") UUID itemId, @Param("status") BatchStatus status);

    /**
     * FIFO query: Get COMPLETE batches ordered by expiration date (earliest first, nulls last).
     * This ensures we consume the earliest expiring batches first.
     */
    @Query(
            """
        SELECT b FROM InventoryBatch b LEFT JOIN FETCH b.item
        WHERE b.itemId = :itemId
        AND b.status = 'COMPLETE'
        AND b.quantity > 0
        ORDER BY CASE WHEN b.expirationDate IS NULL THEN 1 ELSE 0 END,
                 b.expirationDate ASC
        """)
    List<InventoryBatch> findForFifoConsumption(@Param("itemId") UUID itemId);

    /** Find batches expiring before a given date with stock remaining. */
    @Query(
            """
        SELECT b FROM InventoryBatch b LEFT JOIN FETCH b.item
        WHERE b.clinicId = :clinicId
        AND b.quantity > 0
        AND b.expirationDate IS NOT NULL
        AND b.expirationDate <= :date
        ORDER BY b.expirationDate ASC
        """)
    List<InventoryBatch> findExpiringBefore(
            @Param("clinicId") UUID clinicId, @Param("date") LocalDate date);

    /** Find batches expiring within a number of days. */
    @Query(
            """
        SELECT b FROM InventoryBatch b LEFT JOIN FETCH b.item
        WHERE b.clinicId = :clinicId
        AND b.quantity > 0
        AND b.expirationDate IS NOT NULL
        AND b.expirationDate >= CURRENT_DATE
        AND b.expirationDate <= :futureDate
        ORDER BY b.expirationDate ASC
        """)
    List<InventoryBatch> findExpiringSoon(
            @Param("clinicId") UUID clinicId, @Param("futureDate") LocalDate futureDate);

    long countByClinicIdAndStatus(UUID clinicId, BatchStatus status);

    /** Sum quantity for all batches of an item (for stock recalculation). */
    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM InventoryBatch b WHERE b.itemId = :itemId")
    BigDecimal sumQuantityByItemIdDecimal(@Param("itemId") UUID itemId);
}
