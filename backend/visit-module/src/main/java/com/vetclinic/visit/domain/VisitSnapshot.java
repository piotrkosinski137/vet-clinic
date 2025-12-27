package com.vetclinic.visit.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vetclinic.visit.domain.model.Medication;
import com.vetclinic.visit.domain.model.Visit;
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
        LocalDate nextVisitDate,
        List<MedicationSnapshot> medications) {

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
                visit.getMedications() != null
                        ? visit.getMedications().stream().map(MedicationSnapshot::from).toList()
                        : List.of());
    }
}
