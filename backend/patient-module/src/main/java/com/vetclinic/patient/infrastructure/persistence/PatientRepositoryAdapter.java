package com.vetclinic.patient.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.port.PatientRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class PatientRepositoryAdapter implements PatientRepository {

    private final JpaPatientRepository jpaRepository;

    @Override
    public Patient save(Patient patient) {
        return jpaRepository.save(patient);
    }

    @Override
    public Optional<Patient> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Patient> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Patient> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
