package com.vetclinic.veterinarian.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Builder;

@Builder
public record VeterinarianRequest(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid")
                String email,
        String phone,
        String specialization,
        String licenseNumber,
        @Pattern(
                        regexp = "^#[0-9A-Fa-f]{6}$",
                        message = "Color code must be in hex format (e.g., #4CAF50)")
                String colorCode,
        Boolean active,
        String notes) {}
