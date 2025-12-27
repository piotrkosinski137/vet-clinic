package com.vetclinic.billing.domain;

import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.billing.domain.model.InvoiceStatus;

public record InvoiceSearchCriteria(
        UUID clientId, UUID patientId, InvoiceStatus status, LocalDate dateFrom, LocalDate dateTo) {

    public boolean hasAnyCriteria() {
        return clientId != null
                || patientId != null
                || status != null
                || dateFrom != null
                || dateTo != null;
    }
}
