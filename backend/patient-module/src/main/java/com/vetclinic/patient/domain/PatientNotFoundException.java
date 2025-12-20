package com.vetclinic.patient.domain;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID id) {
        super("Patient not found with id: " + id);
    }
}
