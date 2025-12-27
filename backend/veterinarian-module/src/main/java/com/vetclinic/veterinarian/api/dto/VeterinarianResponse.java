package com.vetclinic.veterinarian.api.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record VeterinarianResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String specialization,
        String licenseNumber,
        String colorCode,
        Boolean active,
        String notes,
        Instant createdAt,
        Instant updatedAt) {}
