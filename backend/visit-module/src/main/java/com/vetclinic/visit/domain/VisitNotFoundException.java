package com.vetclinic.visit.domain;

import java.util.UUID;

import com.vetclinic.common.exception.ResourceNotFoundException;

/** Exception thrown when a visit is not found. */
public class VisitNotFoundException extends ResourceNotFoundException {

    public VisitNotFoundException(UUID id) {
        super("Visit", id);
    }
}
