package com.vetclinic.patient.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
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
import com.vetclinic.patient.domain.PatientService;
import com.vetclinic.patient.domain.model.Patient;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request) {
        Patient patient = patientMapper.toEntity(request);
        Patient created = patientService.createPatient(patient);
        PatientResponse response = patientMapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/v1/patients/" + created.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable UUID id) {
        Patient patient = patientService.getPatient(id);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients(
            @RequestParam(required = false) UUID ownerId) {
        List<Patient> patients;
        if (ownerId != null) {
            patients = patientService.getPatientsByOwner(ownerId);
        } else {
            patients = patientService.getAllPatients();
        }
        List<PatientResponse> responses = patients.stream().map(patientMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable UUID id, @Valid @RequestBody PatientRequest request) {
        Patient patient = patientMapper.toEntity(request);
        Patient updated = patientService.updatePatient(id, patient);
        return ResponseEntity.ok(patientMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
