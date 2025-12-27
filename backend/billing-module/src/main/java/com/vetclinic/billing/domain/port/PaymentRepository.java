package com.vetclinic.billing.domain.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.billing.domain.model.Payment;
import com.vetclinic.billing.domain.model.PaymentMethod;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    List<Payment> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    List<Payment> findByInvoiceId(UUID invoiceId);

    List<Payment> findByPaymentMethod(PaymentMethod method);

    List<Payment> findByPaymentDateBetween(LocalDate from, LocalDate to);
}
