package com.vetclinic.billing.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.billing.domain.model.InventoryTransaction;
import com.vetclinic.billing.domain.port.InventoryTransactionRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class InventoryTransactionRepositoryAdapter implements InventoryTransactionRepository {

    private final JpaInventoryTransactionRepository jpaRepository;

    @Override
    public InventoryTransaction save(InventoryTransaction transaction) {
        return jpaRepository.save(transaction);
    }

    @Override
    public List<InventoryTransaction> findByItemId(UUID itemId) {
        return jpaRepository.findByItemId(itemId);
    }

    @Override
    public List<InventoryTransaction> findByClinicIdOrderByCreatedAtDesc(UUID clinicId) {
        return jpaRepository.findByClinicIdOrderByCreatedAtDesc(clinicId);
    }

    @Override
    public List<InventoryTransaction> findByReferenceIdAndReferenceType(
            UUID referenceId, String referenceType) {
        return jpaRepository.findByReferenceIdAndReferenceType(referenceId, referenceType);
    }
}
