package com.vetclinic.visit.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vetclinic.visit.domain.model.Medication;
import com.vetclinic.visit.domain.model.UsedMaterial;
import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitPriority;
import com.vetclinic.visit.domain.model.VisitStatus;

/**
 * Immutable snapshot of a Visit for audit logging. Avoids JPA entity serialization issues and
 * circular references.
 */
public record VisitSnapshot(
        UUID id,
        UUID patientId,
        UUID clientId,
        UUID veterinarianId,
        String veterinarianName,
        LocalDateTime visitDate,
        Integer durationMinutes,
        VisitStatus status,
        String reason,
        String interview,
        String examination,
        String diagnosis,
        String treatment,
        String recommendations,
        String notes,
        Double weight,
        Double temperature,
        LocalDateTime nextVisitDate,
        LocalDateTime checkedInAt,
        String waitingRoomNotes,
        VisitPriority priority,
        List<MedicationSnapshot> medications,
        List<UsedMaterialSnapshot> usedMaterials) {

    public record MedicationSnapshot(
            String name, String dosage, String frequency, String duration, String notes) {
        public static MedicationSnapshot from(Medication med) {
            return new MedicationSnapshot(
                    med.getName(),
                    med.getDosage(),
                    med.getFrequency(),
                    med.getDuration(),
                    med.getNotes());
        }
    }

    public record UsedMaterialSnapshot(
            UUID materialId,
            String name,
            BigDecimal quantity,
            BigDecimal costPrice,
            BigDecimal sellPrice,
            String unit) {
        public static UsedMaterialSnapshot from(UsedMaterial mat) {
            return new UsedMaterialSnapshot(
                    mat.getMaterialId(),
                    mat.getName(),
                    mat.getQuantity(),
                    mat.getCostPrice(),
                    mat.getSellPrice(),
                    mat.getUnit());
        }
    }

    public static VisitSnapshot from(Visit visit) {
        return new VisitSnapshot(
                visit.getId(),
                visit.getPatientId(),
                visit.getClientId(),
                visit.getVeterinarianId(),
                visit.getVeterinarianName(),
                visit.getVisitDate(),
                visit.getDurationMinutes(),
                visit.getStatus(),
                visit.getReason(),
                visit.getInterview(),
                visit.getExamination(),
                visit.getDiagnosis(),
                visit.getTreatment(),
                visit.getRecommendations(),
                visit.getNotes(),
                visit.getWeight(),
                visit.getTemperature(),
                visit.getNextVisitDate(),
                visit.getCheckedInAt(),
                visit.getWaitingRoomNotes(),
                visit.getPriority(),
                visit.getMedications() != null
                        ? visit.getMedications().stream().map(MedicationSnapshot::from).toList()
                        : List.of(),
                visit.getUsedMaterials() != null
                        ? visit.getUsedMaterials().stream().map(UsedMaterialSnapshot::from).toList()
                        : List.of());
    }
}
