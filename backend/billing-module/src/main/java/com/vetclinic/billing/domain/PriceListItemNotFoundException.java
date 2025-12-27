package com.vetclinic.billing.domain;

import java.util.UUID;

import com.vetclinic.common.exception.ResourceNotFoundException;

public class PriceListItemNotFoundException extends ResourceNotFoundException {

    public PriceListItemNotFoundException(UUID id) {
        super("Price list item", id);
    }
}
