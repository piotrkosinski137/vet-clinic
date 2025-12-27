package com.vetclinic.veterinarian.domain;

import java.util.UUID;

public class VeterinarianNotFoundException extends RuntimeException {

    public VeterinarianNotFoundException(UUID id) {
        super("Veterinarian not found with id: " + id);
    }

    public VeterinarianNotFoundException(String email) {
        super("Veterinarian not found with email: " + email);
    }
}
