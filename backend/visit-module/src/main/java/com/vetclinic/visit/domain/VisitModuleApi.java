package com.vetclinic.visit.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Public API for the Visit module.
 *
 * <p>This interface defines the methods that other modules can use to interact with the Visit
 * module. It uses simple DTOs instead of entity types to maintain loose coupling between modules.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @Autowired
 * private VisitModuleApi visitModule;
 *
 * VisitBasicInfo visit = visitModule.getVisitBasicInfo(visitId);
 * long todayCount = visitModule.countVisitsForDate(LocalDate.now());
 * }</pre>
 */
public interface VisitModuleApi {

    /**
     * Get basic visit information by ID.
     *
     * @param visitId the visit ID
     * @return basic visit information
     * @throws com.vetclinic.common.exception.ResourceNotFoundException if visit not found
     */
    VisitBasicInfo getVisitBasicInfo(UUID visitId);

    /**
     * Check if a visit exists by ID.
     *
     * @param visitId the visit ID
     * @return true if visit exists
     */
    boolean existsById(UUID visitId);

    /**
     * Count visits for a specific date.
     *
     * @param date the date to check
     * @return number of visits scheduled for that date
     */
    long countVisitsForDate(LocalDate date);

    /**
     * Get visits for a specific patient.
     *
     * @param patientId the patient ID
     * @return list of visit basic info for the patient
     */
    List<VisitBasicInfo> getVisitsForPatient(UUID patientId);

    /**
     * Get visits for a specific veterinarian on a date.
     *
     * @param veterinarianId the veterinarian ID
     * @param date the date to check
     * @return list of visits for the veterinarian on that date
     */
    List<VisitBasicInfo> getVisitsForVeterinarian(UUID veterinarianId, LocalDate date);

    /**
     * Check if a time slot is available for a veterinarian.
     *
     * @param veterinarianId the veterinarian ID
     * @param date the date to check
     * @param startTime the start time (e.g., "09:00")
     * @param endTime the end time (e.g., "09:30")
     * @return true if the slot is available
     */
    boolean isSlotAvailable(UUID veterinarianId, LocalDate date, String startTime, String endTime);

    /** Basic visit information DTO for inter-module communication. */
    record VisitBasicInfo(
            UUID id,
            UUID patientId,
            String patientName,
            UUID clientId,
            String clientName,
            UUID veterinarianId,
            String veterinarianName,
            LocalDate visitDate,
            String visitTime,
            String status,
            String visitType) {}
}
