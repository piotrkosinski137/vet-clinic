package com.vetclinic.patient.domain;

import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.port.PatientRepository;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public Patient getPatient(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public List<Patient> getPatientsByOwner(UUID ownerId) {
        return patientRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public Patient updatePatient(UUID id, Patient updated) {
        Patient existing = getPatient(id);
        existing.setName(updated.getName());
        existing.setSpecies(updated.getSpecies());
        existing.setBreed(updated.getBreed());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setWeight(updated.getWeight());
        existing.setNotes(updated.getNotes());
        return patientRepository.save(existing);
    }

    @Transactional
    public void deletePatient(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        patientRepository.deleteById(id);
    }
}
