package com.vetclinic.billing.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_INVOICES;
import static com.vetclinic.common.security.Roles.CAN_VIEW_FINANCIALS;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.billing.api.dto.ClientDebtResponse;
import com.vetclinic.billing.api.dto.InvoiceItemDto;
import com.vetclinic.billing.api.dto.InvoicePrintResponse;
import com.vetclinic.billing.api.dto.InvoiceRequest;
import com.vetclinic.billing.api.dto.InvoiceResponse;
import com.vetclinic.billing.api.dto.PaymentRequest;
import com.vetclinic.billing.api.dto.PaymentResponse;
import com.vetclinic.billing.domain.BillingService;
import com.vetclinic.billing.domain.ClientDebtService;
import com.vetclinic.billing.domain.InvoiceSearchCriteria;
import com.vetclinic.billing.domain.PaymentService;
import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceItem;
import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.billing.domain.model.Payment;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final BillingService billingService;
    private final PaymentService paymentService;
    private final ClientDebtService clientDebtService;
    private final InvoiceMapper invoiceMapper;
    private final PaymentMapper paymentMapper;

    @PostMapping
    @PreAuthorize(CAN_MANAGE_INVOICES)
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceRequest request) {
        Invoice invoice = invoiceMapper.toEntity(request);
        Invoice saved = billingService.createInvoice(invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceMapper.toResponse(saved));
    }

    @GetMapping
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<List<InvoiceResponse>> getInvoices(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate dateTo) {
        InvoiceSearchCriteria criteria =
                new InvoiceSearchCriteria(clientId, patientId, status, dateFrom, dateTo);
        List<Invoice> invoices = billingService.searchInvoices(criteria);
        return ResponseEntity.ok(invoiceMapper.toResponseList(invoices));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID id) {
        Invoice invoice = billingService.getInvoice(id);
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @GetMapping("/number/{invoiceNumber}")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        Invoice invoice = billingService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByClient(@PathVariable UUID clientId) {
        List<Invoice> invoices = billingService.getInvoicesByClient(clientId);
        return ResponseEntity.ok(invoiceMapper.toResponseList(invoices));
    }

    @GetMapping("/client/{clientId}/debt")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<ClientDebtResponse> getClientDebt(@PathVariable UUID clientId) {
        ClientDebtService.ClientDebtSummary summary =
                clientDebtService.calculateClientDebt(clientId);
        return ResponseEntity.ok(
                ClientDebtResponse.builder()
                        .clientId(summary.clientId())
                        .totalOutstanding(summary.totalOutstanding())
                        .totalInvoiced(summary.totalInvoiced())
                        .totalPaid(summary.totalPaid())
                        .unpaidInvoiceCount(summary.unpaidInvoiceCount())
                        .overdueInvoiceCount(summary.overdueInvoiceCount())
                        .build());
    }

    @GetMapping("/visit/{visitId}")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByVisit(@PathVariable UUID visitId) {
        List<Invoice> invoices = billingService.getInvoicesByVisit(visitId);
        return ResponseEntity.ok(invoiceMapper.toResponseList(invoices));
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_INVOICES)
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable UUID id, @Valid @RequestBody InvoiceRequest request) {
        Invoice updated = invoiceMapper.toEntity(request);
        Invoice saved = billingService.updateInvoice(id, updated);
        return ResponseEntity.ok(invoiceMapper.toResponse(saved));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize(CAN_MANAGE_INVOICES)
    public ResponseEntity<InvoiceResponse> addItem(
            @PathVariable UUID id, @Valid @RequestBody InvoiceItemDto itemDto) {
        InvoiceItem item = invoiceMapper.toInvoiceItem(itemDto);
        Invoice invoice = billingService.addItem(id, item);
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @PostMapping("/{id}/issue")
    @PreAuthorize(CAN_MANAGE_INVOICES)
    public ResponseEntity<InvoiceResponse> issueInvoice(@PathVariable UUID id) {
        Invoice invoice = billingService.issueInvoice(id);
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize(CAN_MANAGE_INVOICES)
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable UUID id) {
        Invoice invoice = billingService.cancelInvoice(id);
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @GetMapping("/{id}/print")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<InvoicePrintResponse> getInvoicePrint(@PathVariable UUID id) {
        Invoice invoice = billingService.getInvoice(id);
        return ResponseEntity.ok(invoiceMapper.toPrintResponse(invoice));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_INVOICES)
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id) {
        billingService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize(CAN_MANAGE_INVOICES)
    public ResponseEntity<PaymentResponse> recordPayment(
            @PathVariable UUID id, @Valid @RequestBody PaymentRequest request) {
        Payment payment =
                paymentService.recordPayment(
                        id, request.amount(), request.paymentMethod(), request.notes());
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMapper.toResponse(payment));
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<List<PaymentResponse>> getPayments(@PathVariable UUID id) {
        List<Payment> payments = paymentService.getPaymentsForInvoice(id);
        return ResponseEntity.ok(paymentMapper.toResponseList(payments));
    }

    @DeleteMapping("/payments/{paymentId}")
    @PreAuthorize(CAN_MANAGE_INVOICES)
    public ResponseEntity<Void> deletePayment(@PathVariable UUID paymentId) {
        paymentService.deletePayment(paymentId);
        return ResponseEntity.noContent().build();
    }
}
