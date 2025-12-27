package com.vetclinic.veterinarian.domain;

import java.util.UUID;

import com.vetclinic.veterinarian.domain.model.Veterinarian;

/**
 * Immutable snapshot of a Veterinarian for audit logging. Avoids JPA entity serialization issues
 * and circular references.
 */
public record VeterinarianSnapshot(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String specialization,
        String licenseNumber,
        String colorCode,
        Boolean active,
        String notes) {

    public static VeterinarianSnapshot from(Veterinarian vet) {
        return new VeterinarianSnapshot(
                vet.getId(),
                vet.getFirstName(),
                vet.getLastName(),
                vet.getEmail(),
                vet.getPhone(),
                vet.getSpecialization(),
                vet.getLicenseNumber(),
                vet.getColorCode(),
                vet.getActive(),
                vet.getNotes());
    }
}
