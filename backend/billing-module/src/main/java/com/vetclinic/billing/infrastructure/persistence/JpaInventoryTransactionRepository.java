package com.vetclinic.billing.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetclinic.billing.domain.model.InventoryTransaction;

public interface JpaInventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, UUID> {

    List<InventoryTransaction> findByItemId(UUID itemId);

    List<InventoryTransaction> findByClinicIdOrderByCreatedAtDesc(UUID clinicId);

    List<InventoryTransaction> findByReferenceIdAndReferenceType(
            UUID referenceId, String referenceType);
}
