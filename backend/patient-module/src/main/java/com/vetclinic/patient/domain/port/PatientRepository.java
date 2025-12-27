package com.vetclinic.patient.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;

/**
 * Port for patient persistence operations. This interface is implemented by infrastructure layer.
 */
public interface PatientRepository {

    Patient save(Patient patient);

    Optional<Patient> findById(UUID id);

    List<Patient> findAll();

    /** Count total patients */
    long count();

    List<Patient> findByOwnerId(UUID ownerId);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    // Search methods

    /** Find patients by name (case-insensitive partial match) */
    List<Patient> findByNameContainingIgnoreCase(String name);

    /** Find patients by microchip number (exact match) */
    Optional<Patient> findByMicrochipNumber(String microchipNumber);

    /** Find patients by species */
    List<Patient> findBySpecies(Species species);

    /** Find patients that have any of the specified labels */
    List<Patient> findByLabelsIn(Iterable<PatientLabel> labels);

    /** Search patients by multiple criteria */
    List<Patient> search(
            String name, Species species, String breed, UUID ownerId, String microchipNumber);
}
