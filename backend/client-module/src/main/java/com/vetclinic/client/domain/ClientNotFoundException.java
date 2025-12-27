package com.vetclinic.client.domain;

import java.util.UUID;

import com.vetclinic.common.exception.ResourceNotFoundException;

public class ClientNotFoundException extends ResourceNotFoundException {

    public ClientNotFoundException(UUID id) {
        super("Client", id);
    }

    public ClientNotFoundException(String field, String value) {
        super("Client", value, "lookup by " + field);
    }
}
