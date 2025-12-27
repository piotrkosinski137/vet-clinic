package com.vetclinic.client.domain;

import java.util.UUID;

import com.vetclinic.client.domain.model.Client;

/**
 * Immutable snapshot of a Client for audit logging. Avoids JPA entity serialization issues and
 * circular references.
 */
public record ClientSnapshot(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String city,
        String postalCode,
        String notes) {

    public static ClientSnapshot from(Client client) {
        return new ClientSnapshot(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress(),
                client.getCity(),
                client.getPostalCode(),
                client.getNotes());
    }
}
