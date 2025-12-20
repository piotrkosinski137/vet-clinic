package com.vetclinic.client.domain;

import java.util.UUID;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(UUID id) {
        super("Client not found with id: " + id);
    }
}
