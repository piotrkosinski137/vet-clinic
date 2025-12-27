package com.vetclinic.compliance.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.common.tenant.TenantContext;
import com.vetclinic.compliance.domain.model.CertificateType;
import com.vetclinic.compliance.domain.model.VaccinationCertificate;
import com.vetclinic.compliance.domain.port.VaccinationCertificateRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class VaccinationCertificateRepositoryAdapter implements VaccinationCertificateRepository {

    private final JpaVaccinationCertificateRepository jpaRepository;

    @Override
    public VaccinationCertificate save(VaccinationCertificate certificate) {
        if (certificate.getClinicId() == null) {
            certificate.setClinicId(TenantContext.getCurrentClinicId());
        }
        return jpaRepository.save(certificate);
    }

    @Override
    public Optional<VaccinationCertificate> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<VaccinationCertificate> findByCertificateNumber(String certificateNumber) {
        return jpaRepository.findByCertificateNumber(certificateNumber);
    }

    @Override
    public List<VaccinationCertificate> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<VaccinationCertificate> findByPatientId(UUID patientId) {
        return jpaRepository.findByPatientIdOrderByAdministrationDateDesc(patientId);
    }

    @Override
    public List<VaccinationCertificate> findByClientId(UUID clientId) {
        return jpaRepository.findByClientIdOrderByAdministrationDateDesc(clientId);
    }

    @Override
    public List<VaccinationCertificate> findByCertificateType(CertificateType certificateType) {
        return jpaRepository.findByCertificateTypeOrderByAdministrationDateDesc(certificateType);
    }

    @Override
    public List<VaccinationCertificate> findByPatientIdAndCertificateType(
            UUID patientId, CertificateType certificateType) {
        return jpaRepository.findByPatientIdAndCertificateTypeOrderByAdministrationDateDesc(
                patientId, certificateType);
    }

    @Override
    public List<VaccinationCertificate> findValidCertificatesByPatientId(UUID patientId) {
        return jpaRepository.findByPatientIdAndIsValidTrueOrderByAdministrationDateDesc(patientId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
