package com.vetclinic.billing.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.billing.domain.model.Payment;
import com.vetclinic.billing.domain.model.PaymentMethod;
import com.vetclinic.billing.domain.port.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class PaymentRepositoryAdapter implements PaymentRepository {

    private final JpaPaymentRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Payment> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Payment> findByInvoiceId(UUID invoiceId) {
        return jpaRepository.findByInvoiceId(invoiceId);
    }

    @Override
    public List<Payment> findByPaymentMethod(PaymentMethod method) {
        return jpaRepository.findByPaymentMethod(method);
    }

    @Override
    public List<Payment> findByPaymentDateBetween(LocalDate from, LocalDate to) {
        return jpaRepository.findByPaymentDateBetween(from.atStartOfDay(), to.atTime(23, 59, 59));
    }
}
