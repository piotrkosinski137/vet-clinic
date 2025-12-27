package com.vetclinic.billing.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetclinic.billing.domain.model.Payment;
import com.vetclinic.billing.domain.model.PaymentMethod;

public interface JpaPaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByInvoiceId(UUID invoiceId);

    List<Payment> findByPaymentMethod(PaymentMethod method);

    List<Payment> findByPaymentDateBetween(LocalDateTime from, LocalDateTime to);
}
