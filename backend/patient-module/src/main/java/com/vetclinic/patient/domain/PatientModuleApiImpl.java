package com.vetclinic.patient.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.port.PatientRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of the Patient module public API.
 *
 * <p>This class wraps the internal PatientService and PatientRepository to provide a clean,
 * module-level API for other modules to use.
 */
@Component
@RequiredArgsConstructor
public class PatientModuleApiImpl implements PatientModuleApi {

    private final PatientService patientService;
    private final PatientRepository patientRepository;

    @Override
    public PatientBasicInfo getPatientBasicInfo(UUID patientId) {
        var patient = patientService.getPatient(patientId);
        return toBasicInfo(patient);
    }

    @Override
    public boolean existsById(UUID patientId) {
        return patientRepository.existsById(patientId);
    }

    @Override
    public List<PatientBasicInfo> getPatientsByOwner(UUID ownerId) {
        return patientService.getPatientsByOwner(ownerId).stream().map(this::toBasicInfo).toList();
    }

    @Override
    public Optional<PatientBasicInfo> findByMicrochip(String microchipNumber) {
        return patientService.findByMicrochip(microchipNumber).map(this::toBasicInfo);
    }

    @Override
    public long countPatients() {
        return patientService.countPatients();
    }

    private PatientBasicInfo toBasicInfo(Patient patient) {
        return new PatientBasicInfo(
                patient.getId(),
                patient.getName(),
                patient.getSpecies() != null ? patient.getSpecies().name() : null,
                patient.getBreed(),
                patient.getDateOfBirth(),
                patient.getOwnerId(),
                null); // ownerName would need to be fetched from ClientModule
    }
}
