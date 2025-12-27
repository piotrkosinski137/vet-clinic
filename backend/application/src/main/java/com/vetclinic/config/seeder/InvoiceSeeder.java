package com.vetclinic.config.seeder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceItem;
import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.billing.domain.model.Payment;
import com.vetclinic.billing.domain.model.PaymentMethod;

import lombok.extern.slf4j.Slf4j;

/** Seeds invoice and payment entities based on completed visits. */
@Component
@Slf4j
public class InvoiceSeeder implements DataSeeder {

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(23);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int DUE_DAYS = 14;
    private static final int OVERDUE_THRESHOLD_DAYS = 30;

    private static final double NON_TODAY_INVOICE_PROBABILITY = 0.6;
    private static final double FULLY_PAID_PROBABILITY = 0.6;
    private static final double PARTIAL_PAID_PROBABILITY = 0.75;
    private static final double ISSUED_PROBABILITY = 0.9;

    @Override
    public int getOrder() {
        return 7;
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        var invoiceNumber = 1;
        var today = LocalDate.now();
        var paymentMethods = PaymentMethod.values();

        for (var visit : context.getCompletedVisits()) {
            var visitDate = visit.getVisitDate().toLocalDate();
            var isToday = visitDate.equals(today);

            // Skip ~40% of non-today visits
            if (!isToday && context.getRandom().nextDouble() > NON_TODAY_INVOICE_PROBABILITY) {
                continue;
            }

            var invoiceNum = String.format("INV-%d-%04d", today.getYear(), invoiceNumber++);
            var items = generateInvoiceItems(visit.getReason(), context);
            var subtotal =
                    items.stream()
                            .map(InvoiceItem::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            var taxAmount = subtotal.multiply(TAX_RATE).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            var totalAmount = subtotal.add(taxAmount);

            var statusInfo =
                    determinePaymentStatus(isToday, visitDate, today, totalAmount, context);
            var invoice =
                    createInvoice(
                            invoiceNum,
                            visit.getClientId(),
                            visit.getPatientId(),
                            visit.getId(),
                            visitDate,
                            items,
                            subtotal,
                            taxAmount,
                            totalAmount,
                            statusInfo.status,
                            statusInfo.paidAmount,
                            context);
            entityManager.persist(invoice);

            if (statusInfo.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                var payment =
                        createPayment(
                                invoice.getId(),
                                statusInfo.paidAmount,
                                paymentMethods[context.getRandom().nextInt(paymentMethods.length)],
                                isToday,
                                visitDate,
                                context);
                entityManager.persist(payment);
            }
        }
        log.info("Created invoices and payments");
    }

    private java.util.List<InvoiceItem> generateInvoiceItems(String reason, SeedContext context) {
        var items = new ArrayList<InvoiceItem>();
        var random = context.getRandom();

        // Consultation fee
        var consultationFee = BigDecimal.valueOf(100 + random.nextInt(150));
        items.add(
                InvoiceItem.builder()
                        .name("Consultation")
                        .description(reason)
                        .quantity(1)
                        .unitPrice(consultationFee)
                        .total(consultationFee)
                        .build());

        // Random medication
        if (random.nextDouble() > 0.5) {
            var medicationFee = BigDecimal.valueOf(30 + random.nextInt(70));
            var qty = 1 + random.nextInt(2);
            items.add(
                    InvoiceItem.builder()
                            .name("Medication")
                            .description("Prescribed medication")
                            .quantity(qty)
                            .unitPrice(medicationFee)
                            .total(medicationFee.multiply(BigDecimal.valueOf(qty)))
                            .build());
        }
        return items;
    }

    private record PaymentStatusInfo(InvoiceStatus status, BigDecimal paidAmount) {}

    private PaymentStatusInfo determinePaymentStatus(
            boolean isToday,
            LocalDate visitDate,
            LocalDate today,
            BigDecimal totalAmount,
            SeedContext context) {

        if (isToday) {
            return new PaymentStatusInfo(InvoiceStatus.PAID, totalAmount);
        }

        var random = context.getRandom();
        var paymentChance = random.nextDouble();

        if (paymentChance < FULLY_PAID_PROBABILITY) {
            return new PaymentStatusInfo(InvoiceStatus.PAID, totalAmount);
        } else if (paymentChance < PARTIAL_PAID_PROBABILITY) {
            var partialAmount =
                    totalAmount.multiply(BigDecimal.valueOf(0.3 + random.nextDouble() * 0.4));
            return new PaymentStatusInfo(InvoiceStatus.PARTIALLY_PAID, partialAmount);
        } else if (paymentChance < ISSUED_PROBABILITY) {
            return new PaymentStatusInfo(InvoiceStatus.ISSUED, BigDecimal.ZERO);
        } else {
            var status =
                    visitDate.isBefore(today.minusDays(OVERDUE_THRESHOLD_DAYS))
                            ? InvoiceStatus.OVERDUE
                            : InvoiceStatus.ISSUED;
            return new PaymentStatusInfo(status, BigDecimal.ZERO);
        }
    }

    private Invoice createInvoice(
            String invoiceNum,
            UUID clientId,
            UUID patientId,
            UUID visitId,
            LocalDate visitDate,
            java.util.List<InvoiceItem> items,
            BigDecimal subtotal,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            InvoiceStatus status,
            BigDecimal paidAmount,
            SeedContext context) {

        var invoice =
                Invoice.builder()
                        .invoiceNumber(invoiceNum)
                        .clientId(clientId)
                        .patientId(patientId)
                        .visitId(visitId)
                        .issueDate(visitDate)
                        .dueDate(visitDate.plusDays(DUE_DAYS))
                        .status(status)
                        .items(items)
                        .subtotal(subtotal)
                        .taxRate(TAX_RATE)
                        .taxAmount(taxAmount)
                        .totalAmount(totalAmount)
                        .paidAmount(paidAmount)
                        .build();
        invoice.setClinicId(context.getClinicId());
        return invoice;
    }

    private Payment createPayment(
            UUID invoiceId,
            BigDecimal amount,
            PaymentMethod method,
            boolean isToday,
            LocalDate visitDate,
            SeedContext context) {

        var random = context.getRandom();
        var paymentDateTime =
                isToday
                        ? LocalDateTime.now().minusMinutes(random.nextInt(60))
                        : visitDate.atTime(10 + random.nextInt(8), random.nextInt(60));

        var transRef =
                method == PaymentMethod.CARD
                        ? "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                        : null;

        var payment =
                Payment.builder()
                        .invoiceId(invoiceId)
                        .amount(amount)
                        .paymentMethod(method)
                        .paymentDate(paymentDateTime)
                        .transactionReference(transRef)
                        .build();
        payment.setClinicId(context.getClinicId());
        return payment;
    }
}
