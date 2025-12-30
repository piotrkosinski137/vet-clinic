package com.vetclinic.billing.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;
import com.vetclinic.billing.domain.port.InventoryBatchRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class InventoryBatchRepositoryAdapter implements InventoryBatchRepository {

    private final JpaInventoryBatchRepository jpaRepository;

    @Override
    public InventoryBatch save(InventoryBatch batch) {
        return jpaRepository.save(batch);
    }

    @Override
    public Optional<InventoryBatch> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<InventoryBatch> findByClinicId(UUID clinicId) {
        return jpaRepository.findByClinicId(clinicId);
    }

    @Override
    public List<InventoryBatch> findByItemId(UUID itemId) {
        return jpaRepository.findByItemId(itemId);
    }

    @Override
    public List<InventoryBatch> findForFifoConsumption(UUID itemId) {
        return jpaRepository.findForFifoConsumption(itemId);
    }

    @Override
    public List<InventoryBatch> findByClinicIdAndStatus(UUID clinicId, BatchStatus status) {
        return jpaRepository.findByClinicIdAndStatus(clinicId, status);
    }

    @Override
    public List<InventoryBatch> findByItemIdAndStatus(UUID itemId, BatchStatus status) {
        return jpaRepository.findByItemIdAndStatus(itemId, status);
    }

    @Override
    public List<InventoryBatch> findExpiringBefore(UUID clinicId, LocalDate date) {
        return jpaRepository.findExpiringBefore(clinicId, date);
    }

    @Override
    public List<InventoryBatch> findExpiringSoon(UUID clinicId, int days) {
        var futureDate = LocalDate.now().plusDays(days);
        return jpaRepository.findExpiringSoon(clinicId, futureDate);
    }

    @Override
    public long countByClinicIdAndStatus(UUID clinicId, BatchStatus status) {
        return jpaRepository.countByClinicIdAndStatus(clinicId, status);
    }

    @Override
    public BigDecimal sumQuantityByItemId(UUID itemId) {
        var sum = jpaRepository.sumQuantityByItemIdDecimal(itemId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public void delete(InventoryBatch batch) {
        jpaRepository.delete(batch);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
