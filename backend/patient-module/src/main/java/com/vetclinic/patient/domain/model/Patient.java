package com.vetclinic.patient.domain.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A patient (animal) in the veterinary clinic. */
@Entity
@Table(name = "patients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient extends TenantAwareEntity {

    @NotBlank(message = "Patient name is required")
    @Size(max = 100, message = "Patient name must not exceed 100 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Patient species is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Species species;

    @Size(max = 100, message = "Breed must not exceed 100 characters")
    private String breed;

    @PastOrPresent(message = "Date of birth cannot be in the future")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Positive(message = "Weight must be a positive number")
    private Double weight;

    @Column(name = "owner_id")
    private UUID ownerId;

    /** Microchip identification number (ISO 11784/11785 standard, typically 15 digits) */
    @Column(name = "microchip_number", length = 50)
    private String microchipNumber;

    /** Color/markings description for identification */
    @Column(length = 255)
    private String color;

    /** Gender of the patient */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    /** Whether the patient is neutered/spayed */
    @Column(name = "is_neutered")
    private Boolean neutered;

    /** Labels/tags for quick identification and special handling */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_labels", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "label")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<PatientLabel> labels = new HashSet<>();

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    /** Convenience method to add a label */
    public void addLabel(PatientLabel label) {
        if (labels == null) {
            labels = new HashSet<>();
        }
        labels.add(label);
    }

    /** Convenience method to remove a label */
    public void removeLabel(PatientLabel label) {
        if (labels != null) {
            labels.remove(label);
        }
    }

    /** Check if patient has a specific label */
    public boolean hasLabel(PatientLabel label) {
        return labels != null && labels.contains(label);
    }
}
