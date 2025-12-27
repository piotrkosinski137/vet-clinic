package com.vetclinic.compliance.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.compliance.domain.model.CertificateType;
import com.vetclinic.compliance.domain.model.VaccinationCertificate;
import com.vetclinic.compliance.domain.port.VaccinationCertificateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateService {

    private final VaccinationCertificateRepository certificateRepository;
    private final Clock clock;
    private static final AtomicLong certificateCounter =
            new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional
    public VaccinationCertificate createCertificate(VaccinationCertificate certificate) {
        if (certificate.getCertificateNumber() == null) {
            certificate.setCertificateNumber(
                    generateCertificateNumber(certificate.getCertificateType()));
        }
        if (certificate.getAdministrationDate() == null) {
            certificate.setAdministrationDate(LocalDate.now(clock));
        }
        if (certificate.getIsValid() == null) {
            certificate.setIsValid(true);
        }
        return certificateRepository.save(certificate);
    }

    @Transactional
    public VaccinationCertificate updateCertificate(
            UUID id, VaccinationCertificate updatedCertificate) {
        VaccinationCertificate certificate = getCertificate(id);
        certificate.setVaccineName(updatedCertificate.getVaccineName());
        certificate.setVaccineManufacturer(updatedCertificate.getVaccineManufacturer());
        certificate.setBatchNumber(updatedCertificate.getBatchNumber());
        certificate.setExpirationDate(updatedCertificate.getExpirationDate());
        certificate.setNextDueDate(updatedCertificate.getNextDueDate());
        certificate.setNotes(updatedCertificate.getNotes());
        return certificateRepository.save(certificate);
    }

    @Transactional
    public VaccinationCertificate invalidateCertificate(UUID id, String reason) {
        VaccinationCertificate certificate = getCertificate(id);
        certificate.setIsValid(false);
        certificate.setInvalidationReason(reason);
        return certificateRepository.save(certificate);
    }

    public VaccinationCertificate getCertificate(UUID id) {
        return certificateRepository
                .findById(id)
                .orElseThrow(() -> new CertificateNotFoundException(id));
    }

    public VaccinationCertificate getCertificateByNumber(String certificateNumber) {
        return certificateRepository
                .findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new CertificateNotFoundException(certificateNumber));
    }

    public List<VaccinationCertificate> getAllCertificates() {
        return certificateRepository.findAll();
    }

    public List<VaccinationCertificate> getCertificatesByPatient(UUID patientId) {
        return certificateRepository.findByPatientId(patientId);
    }

    public List<VaccinationCertificate> getCertificatesByClient(UUID clientId) {
        return certificateRepository.findByClientId(clientId);
    }

    public List<VaccinationCertificate> getCertificatesByType(CertificateType certificateType) {
        return certificateRepository.findByCertificateType(certificateType);
    }

    public List<VaccinationCertificate> getCertificatesByPatientAndType(
            UUID patientId, CertificateType certificateType) {
        return certificateRepository.findByPatientIdAndCertificateType(patientId, certificateType);
    }

    public List<VaccinationCertificate> getValidCertificatesByPatient(UUID patientId) {
        return certificateRepository.findValidCertificatesByPatientId(patientId);
    }

    @Transactional
    public void deleteCertificate(UUID id) {
        if (certificateRepository.findById(id).isEmpty()) {
            throw new CertificateNotFoundException(id);
        }
        certificateRepository.deleteById(id);
    }

    private String generateCertificateNumber(CertificateType type) {
        String prefix = type != null ? type.name().substring(0, 3) : "CRT";
        String datePrefix = LocalDate.now(clock).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = certificateCounter.incrementAndGet();
        return String.format("%s-%s-%05d", prefix, datePrefix, counter % 100000);
    }
}
