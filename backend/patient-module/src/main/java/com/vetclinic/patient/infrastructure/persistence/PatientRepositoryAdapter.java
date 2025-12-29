package com.vetclinic.patient.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;
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

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Patient> findByNameContainingIgnoreCase(String name) {
        return jpaRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Optional<Patient> findByMicrochipNumber(String microchipNumber) {
        return jpaRepository.findByMicrochipNumber(microchipNumber);
    }

    @Override
    public List<Patient> findBySpecies(Species species) {
        return jpaRepository.findBySpecies(species);
    }

    @Override
    public List<Patient> findByLabelsIn(Iterable<PatientLabel> labels) {
        return jpaRepository.findByLabelsIn(labels);
    }

    @Override
    public List<Patient> search(
            String name, Species species, String breed, UUID ownerId, String microchipNumber) {
        return jpaRepository.search(name, species, breed, ownerId, microchipNumber);
    }

    @Override
    public List<Patient> searchByQuery(String query) {
        return jpaRepository.searchByQuery(query);
    }
}
