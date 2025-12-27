package com.vetclinic.veterinarian.domain;

public class VeterinarianEmailAlreadyExistsException extends RuntimeException {

    public VeterinarianEmailAlreadyExistsException(String email) {
        super("A veterinarian with email '" + email + "' already exists");
    }
}
