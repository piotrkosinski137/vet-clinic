package com.vetclinic.billing.domain;

import java.util.UUID;

import com.vetclinic.common.exception.ResourceNotFoundException;

public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(UUID id) {
        super("Payment", id);
    }
}
