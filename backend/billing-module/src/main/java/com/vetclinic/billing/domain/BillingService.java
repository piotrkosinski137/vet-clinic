package com.vetclinic.billing.domain;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceItem;
import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.billing.domain.port.InvoiceRepository;
import com.vetclinic.common.event.DomainEventPublisher;
import com.vetclinic.common.util.ChangeDetector;

import lombok.RequiredArgsConstructor;

/**
 * Service responsible for invoice management. Handles invoice CRUD operations, status updates, and
 * item management.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingService {

    private static final String ENTITY_TYPE = "Invoice";

    private final InvoiceRepository invoiceRepository;
    private final DomainEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(invoiceRepository.generateNextInvoiceNumber());
        }
        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDate.now(clock));
        }
        invoice.recalculateTotals();
        var saved = invoiceRepository.save(invoice);
        eventPublisher.publishCreated(ENTITY_TYPE, saved.getId(), InvoiceSnapshot.from(saved));
        return saved;
    }

    public Invoice getInvoice(UUID id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new InvoiceNotFoundException(id));
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository
                .findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceNumber));
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getInvoicesByClient(UUID clientId) {
        return invoiceRepository.findByClientId(clientId);
    }

    public List<Invoice> getInvoicesByPatient(UUID patientId) {
        return invoiceRepository.findByPatientId(patientId);
    }

    public List<Invoice> getInvoicesByVisit(UUID visitId) {
        return invoiceRepository.findByVisitId(visitId);
    }

    public List<Invoice> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    public List<Invoice> searchInvoices(InvoiceSearchCriteria criteria) {
        if (!criteria.hasAnyCriteria()) {
            return invoiceRepository.findAll();
        }
        return invoiceRepository.search(
                criteria.clientId(),
                criteria.patientId(),
                criteria.status(),
                criteria.dateFrom(),
                criteria.dateTo());
    }

    @Transactional
    public Invoice updateInvoice(UUID id, Invoice updated) {
        var existing = getInvoice(id);
        if (existing.getStatus() == InvoiceStatus.PAID) {
            throw InvalidInvoiceStateException.cannotModifyPaid(id);
        }

        var oldSnapshot = InvoiceSnapshot.from(existing);
        var changedFields =
                ChangeDetector.comparing(existing, updated)
                        .check("clientId", Invoice::getClientId)
                        .check("patientId", Invoice::getPatientId)
                        .check("visitId", Invoice::getVisitId)
                        .check("dueDate", Invoice::getDueDate)
                        .check("taxRate", Invoice::getTaxRate)
                        .check("notes", Invoice::getNotes)
                        .getChangedFields();

        applyInvoiceUpdates(existing, updated);

        var saved = invoiceRepository.save(existing);
        if (!changedFields.isEmpty()) {
            eventPublisher.publishUpdated(
                    ENTITY_TYPE, id, oldSnapshot, InvoiceSnapshot.from(saved), changedFields);
        }
        return saved;
    }

    private void applyInvoiceUpdates(Invoice existing, Invoice updated) {
        existing.setClientId(updated.getClientId());
        existing.setPatientId(updated.getPatientId());
        existing.setVisitId(updated.getVisitId());
        existing.setDueDate(updated.getDueDate());
        existing.setItems(updated.getItems());
        existing.setTaxRate(updated.getTaxRate());
        existing.setNotes(updated.getNotes());
        existing.recalculateTotals();
    }

    @Transactional
    public Invoice addItem(UUID invoiceId, InvoiceItem item) {
        var invoice = getInvoice(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw InvalidInvoiceStateException.cannotAddItemsToPaid(invoiceId);
        }

        var oldSnapshot = InvoiceSnapshot.from(invoice);
        validateItemFields(item);

        item.setTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        invoice.addItem(item);
        var saved = invoiceRepository.save(invoice);

        eventPublisher.publishUpdated(
                ENTITY_TYPE, invoiceId, oldSnapshot, InvoiceSnapshot.from(saved), Set.of("items"));
        return saved;
    }

    private void validateItemFields(InvoiceItem item) {
        if (item.getQuantity() == null) {
            throw new IllegalArgumentException("Item quantity must not be null");
        }
        if (item.getUnitPrice() == null) {
            throw new IllegalArgumentException("Item unit price must not be null");
        }
    }

    @Transactional
    public Invoice updateStatus(UUID id, InvoiceStatus status) {
        var invoice = getInvoice(id);
        if (Objects.equals(invoice.getStatus(), status)) {
            return invoice;
        }

        var oldSnapshot = InvoiceSnapshot.from(invoice);
        invoice.setStatus(status);
        var saved = invoiceRepository.save(invoice);

        eventPublisher.publishUpdated(
                ENTITY_TYPE, id, oldSnapshot, InvoiceSnapshot.from(saved), Set.of("status"));
        return saved;
    }

    @Transactional
    public Invoice issueInvoice(UUID id) {
        var invoice = getInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw InvalidInvoiceStateException.cannotIssueNonDraft(id, invoice.getStatus());
        }

        var oldSnapshot = InvoiceSnapshot.from(invoice);
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssueDate(LocalDate.now(clock));
        var saved = invoiceRepository.save(invoice);

        eventPublisher.publishUpdated(
                ENTITY_TYPE,
                id,
                oldSnapshot,
                InvoiceSnapshot.from(saved),
                Set.of("status", "issueDate"));
        return saved;
    }

    @Transactional
    public Invoice cancelInvoice(UUID id) {
        var invoice = getInvoice(id);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw InvalidInvoiceStateException.cannotCancelPaid(id);
        }

        var oldSnapshot = InvoiceSnapshot.from(invoice);
        invoice.setStatus(InvoiceStatus.CANCELLED);
        var saved = invoiceRepository.save(invoice);

        eventPublisher.publishUpdated(
                ENTITY_TYPE, id, oldSnapshot, InvoiceSnapshot.from(saved), Set.of("status"));
        return saved;
    }

    @Transactional
    public void deleteInvoice(UUID id) {
        var invoice = getInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw InvalidInvoiceStateException.cannotDeleteNonDraft(id, invoice.getStatus());
        }
        var snapshot = InvoiceSnapshot.from(invoice);
        invoiceRepository.deleteById(id);
        eventPublisher.publishDeleted(ENTITY_TYPE, id, snapshot);
    }
}
