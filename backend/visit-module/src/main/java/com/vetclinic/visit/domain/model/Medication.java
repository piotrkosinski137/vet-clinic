package com.vetclinic.visit.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A prescribed medication during a visit. */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medication {

    @NotBlank(message = "Medication name is required")
    @Size(max = 255, message = "Medication name must not exceed 255 characters")
    @Column(name = "medication_name", nullable = false)
    private String name;

    @Size(max = 100, message = "Dosage must not exceed 100 characters")
    @Column(name = "dosage")
    private String dosage;

    @Size(max = 100, message = "Frequency must not exceed 100 characters")
    @Column(name = "frequency")
    private String frequency;

    @Size(max = 100, message = "Duration must not exceed 100 characters")
    @Column(name = "duration")
    private String duration;

    @Size(max = 500, message = "Medication notes must not exceed 500 characters")
    @Column(name = "medication_notes")
    private String notes;
}
