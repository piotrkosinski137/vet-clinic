package com.vetclinic.client.domain;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Client with email already exists: " + email);
    }
}
