package com.vetclinic.patient.domain.port;

import com.vetclinic.patient.domain.model.Patient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for patient persistence operations.
 * This interface is implemented by infrastructure layer.
 */
public interface PatientRepository {

    Patient save(Patient patient);

    Optional<Patient> findById(UUID id);

    List<Patient> findAll();

    List<Patient> findByOwnerId(UUID ownerId);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
