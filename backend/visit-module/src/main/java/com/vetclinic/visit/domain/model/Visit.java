package com.vetclinic.visit.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A veterinary visit/appointment for a patient. */
@Entity
@Table(name = "visits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visit extends TenantAwareEntity {

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Size(max = 200, message = "Patient name must not exceed 200 characters")
    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "client_id")
    private UUID clientId;

    @Size(max = 200, message = "Client name must not exceed 200 characters")
    @Column(name = "client_name")
    private String clientName;

    @Column(name = "veterinarian_id")
    private UUID veterinarianId;

    @Size(max = 200, message = "Veterinarian name must not exceed 200 characters")
    @Column(name = "veterinarian_name")
    private String veterinarianName;

    @NotNull(message = "Visit date is required")
    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Min(value = 5, message = "Visit duration must be at least 5 minutes")
    @Max(value = 480, message = "Visit duration must not exceed 480 minutes")
    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 30;

    @NotNull(message = "Visit status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VisitStatus status = VisitStatus.SCHEDULED;

    @NotNull(message = "Visit type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false)
    @Builder.Default
    private VisitType visitType = VisitType.CONSULTATION;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    @Column(length = 500)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String interview;

    @Column(columnDefinition = "TEXT")
    private String examination;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "visit_medications", joinColumns = @JoinColumn(name = "visit_id"))
    @Builder.Default
    private List<Medication> medications = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "visit_used_materials", joinColumns = @JoinColumn(name = "visit_id"))
    @Builder.Default
    private List<UsedMaterial> usedMaterials = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Positive(message = "Weight must be a positive number")
    private Double weight;

    @Min(value = 30, message = "Temperature must be at least 30°C")
    @Max(value = 45, message = "Temperature must not exceed 45°C")
    private Double temperature;

    @Column(name = "next_visit_date")
    private LocalDate nextVisitDate;

    // Waiting room fields

    /** Timestamp when the patient was checked into the waiting room */
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    /** Notes specific to the waiting room (e.g., "Patient is anxious", "Owner in a hurry") */
    @Size(max = 500, message = "Waiting room notes must not exceed 500 characters")
    @Column(name = "waiting_room_notes", length = 500)
    private String waitingRoomNotes;

    /** Priority level for the waiting room queue */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    @Builder.Default
    private VisitPriority priority = VisitPriority.NORMAL;

    /** Add a medication to the visit. */
    public void addMedication(Medication medication) {
        if (medications == null) {
            medications = new ArrayList<>();
        }
        medications.add(medication);
    }

    /** Remove a medication from the visit. */
    public void removeMedication(Medication medication) {
        if (medications != null) {
            medications.remove(medication);
        }
    }

    /** Clear all medications. */
    public void clearMedications() {
        if (medications != null) {
            medications.clear();
        }
    }

    /** Calculate end time based on start time and duration. */
    public LocalDateTime getEndTime() {
        return visitDate != null && durationMinutes != null
                ? visitDate.plusMinutes(durationMinutes)
                : visitDate;
    }

    /** Add a material to the visit. */
    public void addUsedMaterial(UsedMaterial material) {
        if (usedMaterials == null) {
            usedMaterials = new ArrayList<>();
        }
        usedMaterials.add(material);
    }

    /** Remove a material from the visit. */
    public void removeUsedMaterial(UsedMaterial material) {
        if (usedMaterials != null) {
            usedMaterials.remove(material);
        }
    }

    /** Clear all used materials. */
    public void clearUsedMaterials() {
        if (usedMaterials != null) {
            usedMaterials.clear();
        }
    }

    /** Calculate total cost of all used materials. */
    public BigDecimal getTotalMaterialsCost() {
        if (usedMaterials == null || usedMaterials.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return usedMaterials.stream()
                .map(UsedMaterial::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Calculate total sell price of all used materials. */
    public BigDecimal getTotalMaterialsSell() {
        if (usedMaterials == null || usedMaterials.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return usedMaterials.stream()
                .map(UsedMaterial::getTotalSell)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Calculate total profit from materials. */
    public BigDecimal getTotalMaterialsProfit() {
        return getTotalMaterialsSell().subtract(getTotalMaterialsCost());
    }
}
