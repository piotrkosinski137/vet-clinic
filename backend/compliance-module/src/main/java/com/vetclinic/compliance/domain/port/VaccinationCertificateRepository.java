package com.vetclinic.compliance.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.compliance.domain.model.CertificateType;
import com.vetclinic.compliance.domain.model.VaccinationCertificate;

public interface VaccinationCertificateRepository {

    VaccinationCertificate save(VaccinationCertificate certificate);

    Optional<VaccinationCertificate> findById(UUID id);

    Optional<VaccinationCertificate> findByCertificateNumber(String certificateNumber);

    List<VaccinationCertificate> findAll();

    List<VaccinationCertificate> findByPatientId(UUID patientId);

    List<VaccinationCertificate> findByClientId(UUID clientId);

    List<VaccinationCertificate> findByCertificateType(CertificateType certificateType);

    List<VaccinationCertificate> findByPatientIdAndCertificateType(
            UUID patientId, CertificateType certificateType);

    List<VaccinationCertificate> findValidCertificatesByPatientId(UUID patientId);

    void deleteById(UUID id);
}
