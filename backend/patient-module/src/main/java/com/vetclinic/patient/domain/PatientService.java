package com.vetclinic.patient.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.common.event.DomainEventPublisher;
import com.vetclinic.common.exception.ResourceNotFoundException;
import com.vetclinic.common.util.ChangeDetector;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;
import com.vetclinic.patient.domain.port.PatientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private static final String ENTITY_TYPE = "Patient";

    private final PatientRepository patientRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public Patient createPatient(Patient patient) {
        var saved = patientRepository.save(patient);
        eventPublisher.publishCreated(ENTITY_TYPE, saved.getId(), saved);
        return saved;
    }

    public Patient getPatient(UUID id) {
        return patientRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public long countPatients() {
        return patientRepository.count();
    }

    public List<Patient> getPatientsByOwner(UUID ownerId) {
        return patientRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public Patient updatePatient(UUID id, Patient updated) {
        var existing = getPatient(id);
        var oldSnapshot = PatientSnapshot.from(existing);

        var changedFields =
                ChangeDetector.comparing(existing, updated)
                        .check("name", Patient::getName)
                        .check("species", Patient::getSpecies)
                        .check("breed", Patient::getBreed)
                        .check("dateOfBirth", Patient::getDateOfBirth)
                        .check("weight", Patient::getWeight)
                        .check("microchipNumber", Patient::getMicrochipNumber)
                        .check("color", Patient::getColor)
                        .check("gender", Patient::getGender)
                        .check("neutered", Patient::getNeutered)
                        .check("notes", Patient::getNotes)
                        .getChangedFields();

        applyPatientUpdates(existing, updated);

        var saved = patientRepository.save(existing);
        if (!changedFields.isEmpty()) {
            eventPublisher.publishUpdated(
                    ENTITY_TYPE, id, oldSnapshot, PatientSnapshot.from(saved), changedFields);
        }
        return saved;
    }

    private void applyPatientUpdates(Patient existing, Patient updated) {
        existing.setName(updated.getName());
        existing.setSpecies(updated.getSpecies());
        existing.setBreed(updated.getBreed());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setWeight(updated.getWeight());
        existing.setMicrochipNumber(updated.getMicrochipNumber());
        existing.setColor(updated.getColor());
        existing.setGender(updated.getGender());
        existing.setNeutered(updated.getNeutered());
        existing.setLabels(updated.getLabels());
        existing.setNotes(updated.getNotes());
    }

    @Transactional
    public void deletePatient(UUID id) {
        var patient =
                patientRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        patientRepository.deleteById(id);
        eventPublisher.publishDeleted(ENTITY_TYPE, id, PatientSnapshot.from(patient));
    }

    // Search methods

    /** Search patients by name (case-insensitive partial match) */
    public List<Patient> searchByName(String name) {
        return patientRepository.findByNameContainingIgnoreCase(name);
    }

    /** Find patient by microchip number (exact match) */
    public Optional<Patient> findByMicrochip(String microchipNumber) {
        return patientRepository.findByMicrochipNumber(microchipNumber);
    }

    /** Get patient by microchip, throwing exception if not found */
    public Patient getByMicrochip(String microchipNumber) {
        return patientRepository
                .findByMicrochipNumber(microchipNumber)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Patient with microchip", microchipNumber));
    }

    /** Find patients by species */
    public List<Patient> getPatientsBySpecies(Species species) {
        return patientRepository.findBySpecies(species);
    }

    /** Find patients that have any of the specified labels */
    public List<Patient> getPatientsByLabels(Iterable<PatientLabel> labels) {
        return patientRepository.findByLabelsIn(labels);
    }

    /** Search patients by multiple criteria */
    public List<Patient> searchPatients(PatientSearchCriteria criteria) {
        Objects.requireNonNull(criteria, "Search criteria must not be null");
        if (!criteria.hasAnyCriteria()) {
            return patientRepository.findAll();
        }
        return patientRepository.search(
                criteria.name(),
                criteria.species(),
                criteria.breed(),
                criteria.ownerId(),
                criteria.microchipNumber());
    }

    /** Add a label to a patient */
    @Transactional
    public Patient addLabel(UUID patientId, PatientLabel label) {
        var patient = getPatient(patientId);
        patient.addLabel(label);
        return patientRepository.save(patient);
    }

    /** Remove a label from a patient */
    @Transactional
    public Patient removeLabel(UUID patientId, PatientLabel label) {
        var patient = getPatient(patientId);
        patient.removeLabel(label);
        return patientRepository.save(patient);
    }
}
