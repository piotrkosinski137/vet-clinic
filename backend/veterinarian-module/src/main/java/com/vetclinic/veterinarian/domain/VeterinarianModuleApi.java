package com.vetclinic.veterinarian.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Public API for the Veterinarian module.
 *
 * <p>This interface defines the methods that other modules can use to interact with the
 * Veterinarian module. It uses simple DTOs instead of entity types to maintain loose coupling
 * between modules.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @Autowired
 * private VeterinarianModuleApi vetModule;
 *
 * VetBasicInfo vet = vetModule.getVeterinarianBasicInfo(vetId);
 * boolean available = vetModule.isVeterinarianAvailable(vetId, date, startTime, endTime);
 * }</pre>
 */
public interface VeterinarianModuleApi {

    /**
     * Get basic veterinarian information by ID.
     *
     * @param veterinarianId the veterinarian ID
     * @return basic veterinarian information
     * @throws com.vetclinic.common.exception.ResourceNotFoundException if veterinarian not found
     */
    VetBasicInfo getVeterinarianBasicInfo(UUID veterinarianId);

    /**
     * Check if a veterinarian exists by ID.
     *
     * @param veterinarianId the veterinarian ID
     * @return true if veterinarian exists
     */
    boolean existsById(UUID veterinarianId);

    /**
     * Get all active veterinarians.
     *
     * @return list of active veterinarians
     */
    List<VetBasicInfo> getAllActiveVeterinarians();

    /**
     * Check if a veterinarian is available at a given date and time slot.
     *
     * @param veterinarianId the veterinarian ID
     * @param date the date to check
     * @param startTime the start time of the slot
     * @param endTime the end time of the slot
     * @return true if the veterinarian is available
     */
    boolean isVeterinarianAvailable(
            UUID veterinarianId, LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * Get veterinarians available for a specific date.
     *
     * @param date the date to check
     * @return list of available veterinarians with their time slots
     */
    List<VetAvailability> getAvailableVeterinarians(LocalDate date);

    /** Basic veterinarian information DTO for inter-module communication. */
    record VetBasicInfo(
            UUID id,
            String firstName,
            String lastName,
            String specialization,
            String email,
            boolean active) {

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    /** Veterinarian availability information for a specific date. */
    record VetAvailability(
            UUID veterinarianId,
            String veterinarianName,
            LocalDate date,
            List<TimeSlot> availableSlots) {}

    /** A time slot. */
    record TimeSlot(LocalTime startTime, LocalTime endTime) {}
}
