package com.vetclinic.billing.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.billing.domain.model.SupplierInvoice;
import com.vetclinic.billing.domain.model.SupplierInvoiceStatus;
import com.vetclinic.billing.domain.port.SupplierInvoiceRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class SupplierInvoiceRepositoryAdapter implements SupplierInvoiceRepository {

    private final JpaSupplierInvoiceRepository jpaRepository;

    @Override
    public SupplierInvoice save(SupplierInvoice invoice) {
        return jpaRepository.save(invoice);
    }

    @Override
    public Optional<SupplierInvoice> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<SupplierInvoice> findByClinicIdOrderByInvoiceDateDesc(UUID clinicId) {
        return jpaRepository.findByClinicIdOrderByInvoiceDateDesc(clinicId);
    }

    @Override
    public List<SupplierInvoice> findByStatus(SupplierInvoiceStatus status) {
        if (status == null) {
            return jpaRepository.findAllOrderByInvoiceDateDesc();
        }
        return jpaRepository.findByStatus(status);
    }
}
