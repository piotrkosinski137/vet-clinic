package com.vetclinic.billing.infrastructure.persistence;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.billing.domain.port.InvoiceRepository;
import com.vetclinic.common.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final JpaInvoiceRepository jpaRepository;

    @Override
    public Invoice save(Invoice invoice) {
        return jpaRepository.save(invoice);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return jpaRepository.findByInvoiceNumber(invoiceNumber);
    }

    @Override
    public List<Invoice> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Invoice> findByClientId(UUID clientId) {
        return jpaRepository.findByClientId(clientId);
    }

    @Override
    public List<Invoice> findByPatientId(UUID patientId) {
        return jpaRepository.findByPatientId(patientId);
    }

    @Override
    public List<Invoice> findByVisitId(UUID visitId) {
        return jpaRepository.findByVisitId(visitId);
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<Invoice> findByIssueDateBetween(LocalDate from, LocalDate to) {
        return jpaRepository.findByIssueDateBetween(from, to);
    }

    @Override
    public List<Invoice> search(
            UUID clientId,
            UUID patientId,
            InvoiceStatus status,
            LocalDate dateFrom,
            LocalDate dateTo) {
        return jpaRepository.search(clientId, patientId, status, dateFrom, dateTo);
    }

    @Override
    public String generateNextInvoiceNumber() {
        String prefix = "INV-" + Year.now().getValue() + "-";
        UUID clinicId = TenantContext.getCurrentClinicId();
        Integer nextSeq = jpaRepository.getNextSequenceNumber(prefix, clinicId);
        return prefix + String.format("%05d", nextSeq);
    }
}
