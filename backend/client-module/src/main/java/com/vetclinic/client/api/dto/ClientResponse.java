package com.vetclinic.client.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String city,
        String postalCode,
        String notes,
        Instant createdAt,
        Instant updatedAt) {}
