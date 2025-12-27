package com.vetclinic.billing.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceStatus;

public interface JpaInvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByClientId(UUID clientId);

    List<Invoice> findByPatientId(UUID patientId);

    List<Invoice> findByVisitId(UUID visitId);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByIssueDateBetween(LocalDate from, LocalDate to);

    @Query(
            """
            SELECT i FROM Invoice i
            WHERE (:clientId IS NULL OR i.clientId = :clientId)
            AND (:patientId IS NULL OR i.patientId = :patientId)
            AND (:status IS NULL OR i.status = :status)
            AND (:dateFrom IS NULL OR i.issueDate >= :dateFrom)
            AND (:dateTo IS NULL OR i.issueDate <= :dateTo)
            ORDER BY i.issueDate DESC
            """)
    List<Invoice> search(
            @Param("clientId") UUID clientId,
            @Param("patientId") UUID patientId,
            @Param("status") InvoiceStatus status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query(
            value =
                    """
            SELECT COALESCE(MAX(CAST(SUBSTRING(invoice_number, 5) AS INTEGER)), 0) + 1
            FROM invoices
            WHERE invoice_number LIKE CONCAT(:prefix, '%')
            AND clinic_id = :clinicId
            """,
            nativeQuery = true)
    Integer getNextSequenceNumber(@Param("prefix") String prefix, @Param("clinicId") UUID clinicId);
}
