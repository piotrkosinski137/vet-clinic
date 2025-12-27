package com.vetclinic.patient.domain;

import java.util.UUID;

import com.vetclinic.common.exception.ResourceNotFoundException;

public class PatientNotFoundException extends ResourceNotFoundException {

    public PatientNotFoundException(UUID id) {
        super("Patient", id);
    }
}
