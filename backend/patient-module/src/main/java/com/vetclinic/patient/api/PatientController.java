package com.vetclinic.patient.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_PATIENTS;
import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.patient.api.dto.PatientRequest;
import com.vetclinic.patient.api.dto.PatientResponse;
import com.vetclinic.patient.domain.PatientSearchCriteria;
import com.vetclinic.patient.domain.PatientService;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @PostMapping
    @PreAuthorize(CAN_MANAGE_PATIENTS)
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request) {
        Patient patient = patientMapper.toEntity(request);
        Patient created = patientService.createPatient(patient);
        PatientResponse response = patientMapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/v1/patients/" + created.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<PatientResponse> getPatient(@PathVariable UUID id) {
        Patient patient = patientService.getPatient(id);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }

    /**
     * Get all patients with optional filtering.
     *
     * @param q Full-text search query (searches patient name, species, breed, AND owner name) Uses
     *     diacritic-insensitive search (e.g., "Wozniak" finds "Woźniak")
     * @param ownerId Filter by owner ID
     * @param name Search by name (partial, case-insensitive)
     * @param species Filter by species
     * @param breed Search by breed (partial, case-insensitive)
     * @param microchipNumber Search by microchip number (exact match)
     * @param labels Filter by labels (patients with ANY of these labels)
     */
    @GetMapping
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<PatientResponse>> getAllPatients(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Species species,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String microchipNumber,
            @RequestParam(required = false) Set<PatientLabel> labels) {

        List<Patient> patients;

        // If 'q' parameter is provided, use full-text search
        if (q != null && !q.isBlank()) {
            patients = patientService.searchByQuery(q);
        } else {
            PatientSearchCriteria criteria =
                    new PatientSearchCriteria(
                            name, species, breed, ownerId, microchipNumber, labels);

            if (criteria.hasAnyCriteria()) {
                patients = patientService.searchPatients(criteria);
            } else {
                patients = patientService.getAllPatients();
            }
        }

        List<PatientResponse> responses = patients.stream().map(patientMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /** Search patients by microchip number. */
    @GetMapping("/microchip/{microchipNumber}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<PatientResponse> getByMicrochip(@PathVariable String microchipNumber) {
        Patient patient = patientService.getByMicrochip(microchipNumber);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_PATIENTS)
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable UUID id, @Valid @RequestBody PatientRequest request) {
        Patient patient = patientMapper.toEntity(request);
        Patient updated = patientService.updatePatient(id, patient);
        return ResponseEntity.ok(patientMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_PATIENTS)
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    /** Add a label to a patient. */
    @PostMapping("/{id}/labels/{label}")
    @PreAuthorize(CAN_MANAGE_PATIENTS)
    public ResponseEntity<PatientResponse> addLabel(
            @PathVariable UUID id, @PathVariable PatientLabel label) {
        Patient patient = patientService.addLabel(id, label);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }

    /** Remove a label from a patient. */
    @DeleteMapping("/{id}/labels/{label}")
    @PreAuthorize(CAN_MANAGE_PATIENTS)
    public ResponseEntity<PatientResponse> removeLabel(
            @PathVariable UUID id, @PathVariable PatientLabel label) {
        Patient patient = patientService.removeLabel(id, label);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }
}
