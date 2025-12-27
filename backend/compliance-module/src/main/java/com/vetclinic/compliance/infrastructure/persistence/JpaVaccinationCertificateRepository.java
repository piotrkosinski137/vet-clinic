package com.vetclinic.compliance.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetclinic.compliance.domain.model.CertificateType;
import com.vetclinic.compliance.domain.model.VaccinationCertificate;

public interface JpaVaccinationCertificateRepository
        extends JpaRepository<VaccinationCertificate, UUID> {

    Optional<VaccinationCertificate> findByCertificateNumber(String certificateNumber);

    List<VaccinationCertificate> findByPatientIdOrderByAdministrationDateDesc(UUID patientId);

    List<VaccinationCertificate> findByClientIdOrderByAdministrationDateDesc(UUID clientId);

    List<VaccinationCertificate> findByCertificateTypeOrderByAdministrationDateDesc(
            CertificateType certificateType);

    List<VaccinationCertificate> findByPatientIdAndCertificateTypeOrderByAdministrationDateDesc(
            UUID patientId, CertificateType certificateType);

    List<VaccinationCertificate> findByPatientIdAndIsValidTrueOrderByAdministrationDateDesc(
            UUID patientId);
}
