package com.vetclinic.compliance.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_PATIENTS;
import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
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

import com.vetclinic.compliance.api.dto.VaccinationCertificateRequest;
import com.vetclinic.compliance.api.dto.VaccinationCertificateResponse;
import com.vetclinic.compliance.domain.CertificateService;
import com.vetclinic.compliance.domain.model.CertificateType;
import com.vetclinic.compliance.domain.model.VaccinationCertificate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class VaccinationCertificateController {

    private final CertificateService certificateService;
    private final VaccinationCertificateMapper mapper;

    @PostMapping
    @PreAuthorize(CAN_MANAGE_PATIENTS)
    public ResponseEntity<VaccinationCertificateResponse> createCertificate(
            @Valid @RequestBody VaccinationCertificateRequest request) {
        VaccinationCertificate certificate = mapper.toEntity(request);
        VaccinationCertificate created = certificateService.createCertificate(certificate);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VaccinationCertificateResponse>> getAllCertificates() {
        List<VaccinationCertificate> certificates = certificateService.getAllCertificates();
        return ResponseEntity.ok(mapper.toResponseList(certificates));
    }

    @GetMapping("/{id}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<VaccinationCertificateResponse> getCertificate(@PathVariable UUID id) {
        VaccinationCertificate certificate = certificateService.getCertificate(id);
        return ResponseEntity.ok(mapper.toResponse(certificate));
    }

    @GetMapping("/number/{certificateNumber}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<VaccinationCertificateResponse> getCertificateByNumber(
            @PathVariable String certificateNumber) {
        VaccinationCertificate certificate =
                certificateService.getCertificateByNumber(certificateNumber);
        return ResponseEntity.ok(mapper.toResponse(certificate));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VaccinationCertificateResponse>> getCertificatesByPatient(
            @PathVariable UUID patientId) {
        List<VaccinationCertificate> certificates =
                certificateService.getCertificatesByPatient(patientId);
        return ResponseEntity.ok(mapper.toResponseList(certificates));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VaccinationCertificateResponse>> getCertificatesByClient(
            @PathVariable UUID clientId) {
        List<VaccinationCertificate> certificates =
                certificateService.getCertificatesByClient(clientId);
        return ResponseEntity.ok(mapper.toResponseList(certificates));
    }

    @GetMapping("/type/{certificateType}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VaccinationCertificateResponse>> getCertificatesByType(
            @PathVariable CertificateType certificateType) {
        List<VaccinationCertificate> certificates =
                certificateService.getCertificatesByType(certificateType);
        return ResponseEntity.ok(mapper.toResponseList(certificates));
    }

    @GetMapping("/patient/{patientId}/type/{certificateType}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VaccinationCertificateResponse>> getCertificatesByPatientAndType(
            @PathVariable UUID patientId, @PathVariable CertificateType certificateType) {
        List<VaccinationCertificate> certificates =
                certificateService.getCertificatesByPatientAndType(patientId, certificateType);
        return ResponseEntity.ok(mapper.toResponseList(certificates));
    }

    @GetMapping("/patient/{patientId}/valid")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VaccinationCertificateResponse>> getValidCertificatesByPatient(
            @PathVariable UUID patientId) {
        List<VaccinationCertificate> certificates =
                certificateService.getValidCertificatesByPatient(patientId);
        return ResponseEntity.ok(mapper.toResponseList(certificates));
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_PATIENTS)
    public ResponseEntity<VaccinationCertificateResponse> updateCertificate(
            @PathVariable UUID id, @Valid @RequestBody VaccinationCertificateRequest request) {
        VaccinationCertificate updatedCertificate = mapper.toEntity(request);
        VaccinationCertificate certificate =
                certificateService.updateCertificate(id, updatedCertificate);
        return ResponseEntity.ok(mapper.toResponse(certificate));
    }

    @PutMapping("/{id}/invalidate")
    @PreAuthorize(CAN_MANAGE_PATIENTS)
    public ResponseEntity<VaccinationCertificateResponse> invalidateCertificate(
            @PathVariable UUID id, @RequestParam String reason) {
        VaccinationCertificate certificate = certificateService.invalidateCertificate(id, reason);
        return ResponseEntity.ok(mapper.toResponse(certificate));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_PATIENTS)
    public ResponseEntity<Void> deleteCertificate(@PathVariable UUID id) {
        certificateService.deleteCertificate(id);
        return ResponseEntity.noContent().build();
    }
}
