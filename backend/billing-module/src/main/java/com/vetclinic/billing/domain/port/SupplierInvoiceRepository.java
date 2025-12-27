package com.vetclinic.billing.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.billing.domain.model.SupplierInvoice;
import com.vetclinic.billing.domain.model.SupplierInvoiceStatus;

public interface SupplierInvoiceRepository {

    SupplierInvoice save(SupplierInvoice invoice);

    Optional<SupplierInvoice> findById(UUID id);

    List<SupplierInvoice> findByClinicIdOrderByInvoiceDateDesc(UUID clinicId);

    List<SupplierInvoice> findByStatus(SupplierInvoiceStatus status);
}
