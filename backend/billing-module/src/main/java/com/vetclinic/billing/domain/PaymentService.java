package com.vetclinic.billing.domain;

import static com.vetclinic.common.validation.ValidationUtils.requirePositive;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.billing.domain.model.Payment;
import com.vetclinic.billing.domain.model.PaymentMethod;
import com.vetclinic.billing.domain.port.InvoiceRepository;
import com.vetclinic.billing.domain.port.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing payments. Handles payment recording, deletion, and payment
 * queries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    /**
     * Record a payment for an invoice. Validates that the payment amount is positive and does not
     * exceed the remaining amount.
     *
     * @param invoiceId the invoice ID
     * @param amount the payment amount (must be > 0 and <= remainingAmount)
     * @param method the payment method
     * @param notes optional payment notes
     * @return the saved payment
     * @throws IllegalArgumentException if amount is invalid or exceeds remaining amount
     * @throws InvalidInvoiceStatusException if invoice status does not allow payments
     */
    @Transactional
    public Payment recordPayment(
            UUID invoiceId, BigDecimal amount, PaymentMethod method, String notes) {
        requirePositive(amount, "Payment amount");

        var invoice =
                invoiceRepository
                        .findById(invoiceId)
                        .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        // Validate invoice status allows payment
        InvoiceStatusValidator.requirePayable(invoice.getStatus());

        // Calculate remaining amount to prevent overpayment
        var remainingAmount = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (amount.compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount ("
                            + amount
                            + ") exceeds remaining amount ("
                            + remainingAmount
                            + ")");
        }

        var payment =
                Payment.builder()
                        .invoiceId(invoiceId)
                        .amount(amount)
                        .paymentMethod(method)
                        .paymentDate(LocalDateTime.now(clock))
                        .notes(notes)
                        .build();

        var saved = paymentRepository.save(payment);

        // Update invoice paid amount and status - calculate once to avoid race condition
        var newPaidAmount = invoice.getPaidAmount().add(amount);
        invoice.setPaidAmount(newPaidAmount);
        updateInvoiceStatusAfterPayment(invoice, newPaidAmount);
        invoiceRepository.save(invoice);

        log.debug(
                "Recorded payment of {} for invoice {}, new status: {}",
                amount,
                invoiceId,
                invoice.getStatus());

        return saved;
    }

    private void updateInvoiceStatusAfterPayment(Invoice invoice, BigDecimal newPaidAmount) {
        if (newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
    }

    /**
     * Delete a payment and update the invoice accordingly.
     *
     * @param paymentId the payment ID
     * @throws PaymentNotFoundException if payment not found
     */
    @Transactional
    public void deletePayment(UUID paymentId) {
        var payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        var invoice =
                invoiceRepository
                        .findById(payment.getInvoiceId())
                        .orElseThrow(() -> new InvoiceNotFoundException(payment.getInvoiceId()));

        // Calculate and update invoice paid amount
        var newPaidAmount = invoice.getPaidAmount().subtract(payment.getAmount());
        if (newPaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setPaidAmount(BigDecimal.ZERO);
            invoice.setStatus(InvoiceStatus.ISSUED);
        } else {
            invoice.setPaidAmount(newPaidAmount);
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(invoice);

        paymentRepository.deleteById(paymentId);
        log.debug("Deleted payment {} from invoice {}", paymentId, payment.getInvoiceId());
    }

    /**
     * Get all payments for a specific invoice.
     *
     * @param invoiceId the invoice ID
     * @return list of payments for the invoice
     */
    public List<Payment> getPaymentsForInvoice(UUID invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    /**
     * Get a payment by ID.
     *
     * @param paymentId the payment ID
     * @return the payment
     * @throws PaymentNotFoundException if payment not found
     */
    public Payment getPayment(UUID paymentId) {
        return paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }
}
