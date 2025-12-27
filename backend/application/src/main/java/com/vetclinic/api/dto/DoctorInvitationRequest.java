package com.vetclinic.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

/**
 * Request DTO for inviting a new doctor to the clinic. The doctor will receive an email invitation
 * to set up their password.
 */
@Builder
public record DoctorInvitationRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid")
                String email,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        String phone,
        String specialization,
        String licenseNumber,
        String notes) {}
