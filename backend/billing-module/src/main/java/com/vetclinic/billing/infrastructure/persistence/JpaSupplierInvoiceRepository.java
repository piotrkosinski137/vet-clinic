package com.vetclinic.billing.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.vetclinic.billing.domain.model.SupplierInvoice;
import com.vetclinic.billing.domain.model.SupplierInvoiceStatus;

public interface JpaSupplierInvoiceRepository extends JpaRepository<SupplierInvoice, UUID> {

    List<SupplierInvoice> findByClinicIdOrderByInvoiceDateDesc(UUID clinicId);

    List<SupplierInvoice> findByStatus(SupplierInvoiceStatus status);

    @Query("SELECT s FROM SupplierInvoice s ORDER BY s.invoiceDate DESC")
    List<SupplierInvoice> findAllOrderByInvoiceDateDesc();

    /** Find invoice by ID with items eagerly fetched to prevent N+1 queries. */
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT s FROM SupplierInvoice s WHERE s.id = :id")
    Optional<SupplierInvoice> findByIdWithItems(UUID id);

    /** Find all invoices with items eagerly fetched to prevent N+1 queries. */
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT s FROM SupplierInvoice s ORDER BY s.invoiceDate DESC")
    List<SupplierInvoice> findAllWithItems();
}
