package com.vetclinic.billing.domain.port;

import java.util.List;
import java.util.UUID;

import com.vetclinic.billing.domain.model.InventoryTransaction;

public interface InventoryTransactionRepository {

    InventoryTransaction save(InventoryTransaction transaction);

    List<InventoryTransaction> findByItemId(UUID itemId);

    List<InventoryTransaction> findByClinicIdOrderByCreatedAtDesc(UUID clinicId);

    List<InventoryTransaction> findByReferenceIdAndReferenceType(
            UUID referenceId, String referenceType);
}
