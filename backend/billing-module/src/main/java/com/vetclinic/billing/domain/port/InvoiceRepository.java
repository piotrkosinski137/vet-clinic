package com.vetclinic.billing.domain.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceStatus;

public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(UUID id);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    List<Invoice> findByClientId(UUID clientId);

    List<Invoice> findByPatientId(UUID patientId);

    List<Invoice> findByVisitId(UUID visitId);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByIssueDateBetween(LocalDate from, LocalDate to);

    List<Invoice> search(
            UUID clientId,
            UUID patientId,
            InvoiceStatus status,
            LocalDate dateFrom,
            LocalDate dateTo);

    String generateNextInvoiceNumber();
}
