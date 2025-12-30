package com.vetclinic.patient.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API for the Patient module.
 *
 * <p>This interface defines the methods that other modules can use to interact with the Patient
 * module. It uses simple DTOs instead of entity types to maintain loose coupling between modules.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @Autowired
 * private PatientModuleApi patientModule;
 *
 * PatientBasicInfo patient = patientModule.getPatientBasicInfo(patientId);
 * }</pre>
 */
public interface PatientModuleApi {

    /**
     * Get basic patient information by ID.
     *
     * @param patientId the patient ID
     * @return basic patient information
     * @throws com.vetclinic.common.exception.ResourceNotFoundException if patient not found
     */
    PatientBasicInfo getPatientBasicInfo(UUID patientId);

    /**
     * Check if a patient exists by ID.
     *
     * @param patientId the patient ID
     * @return true if patient exists
     */
    boolean existsById(UUID patientId);

    /**
     * Get all patients for a given owner/client.
     *
     * @param ownerId the owner/client ID
     * @return list of patient basic info
     */
    List<PatientBasicInfo> getPatientsByOwner(UUID ownerId);

    /**
     * Find patient by microchip number.
     *
     * @param microchipNumber the microchip number
     * @return optional containing patient info if found
     */
    Optional<PatientBasicInfo> findByMicrochip(String microchipNumber);

    /**
     * Get total count of patients.
     *
     * @return patient count
     */
    long countPatients();

    /** Basic patient information DTO for inter-module communication. */
    record PatientBasicInfo(
            UUID id,
            String name,
            String species,
            String breed,
            LocalDate dateOfBirth,
            UUID ownerId,
            String ownerName) {}
}
