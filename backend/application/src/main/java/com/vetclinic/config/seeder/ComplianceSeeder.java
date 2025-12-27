package com.vetclinic.config.seeder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.compliance.domain.model.CertificateType;
import com.vetclinic.compliance.domain.model.ConsentStatus;
import com.vetclinic.compliance.domain.model.ConsentType;
import com.vetclinic.compliance.domain.model.GdprConsent;
import com.vetclinic.compliance.domain.model.VaccinationCertificate;

import lombok.extern.slf4j.Slf4j;

/** Seeds GDPR consent and vaccination certificate entities. */
@Component
@Slf4j
public class ComplianceSeeder implements DataSeeder {

    private static final int DAYS_IN_YEAR = 365;
    private static final int CONSENT_EXPIRY_YEARS = 2;
    private static final int CERT_EXPIRY_YEARS = 1;
    private static final int CERT_DUE_BEFORE_DAYS = 30;

    private static final double CERT_PROBABILITY = 0.7;
    private static final double GRANTED_PROBABILITY = 0.75;
    private static final double REVOKED_PROBABILITY = 0.85;
    private static final double PENDING_PROBABILITY = 0.95;

    private static final String[] VACCINE_NAMES = {
        "Rabies Vaccine",
        "DHPP (Distemper, Hepatitis, Parainfluenza, Parvovirus)",
        "FVRCP (Feline Viral Rhinotracheitis, Calicivirus, Panleukopenia)",
        "Bordetella (Kennel Cough)",
        "Leptospirosis Vaccine",
        "Lyme Disease Vaccine"
    };

    private static final String[] MANUFACTURERS = {
        "Zoetis", "Merck Animal Health", "Boehringer Ingelheim", "Elanco"
    };

    private static final String[] CONSENT_TEXTS = {
        "I consent to the processing of my personal data for veterinary services.",
        "I consent to receive marketing communications about clinic services and promotions.",
        "I consent to the use of photos/videos of my pet for clinic promotional materials.",
        "I consent to medical procedures as recommended by the veterinarian.",
        "I consent to anesthesia administration if required for procedures.",
        "I consent to sharing medical data with specialist veterinarians if needed.",
        "I consent to the use of anonymized data for veterinary research."
    };

    @Override
    public int getOrder() {
        return 8;
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        seedCertificates(entityManager, context);
        seedGdprConsents(entityManager, context);
        log.info("Created vaccination certificates and GDPR consents");
    }

    private void seedCertificates(EntityManager entityManager, SeedContext context) {
        var certNumber = 1;
        var random = context.getRandom();
        var today = LocalDate.now();

        for (var patient : context.getPatients()) {
            if (random.nextDouble() > CERT_PROBABILITY) continue;

            var clientOpt =
                    context.getClients().stream()
                            .filter(c -> c.getId().equals(patient.getOwnerId()))
                            .findFirst();
            if (clientOpt.isEmpty()) continue;
            var client = clientOpt.get();

            var numCertificates = 1 + random.nextInt(3);
            for (int i = 0; i < numCertificates; i++) {
                var certNum = String.format("CERT-%d-%05d", today.getYear(), certNumber++);
                var adminDate = today.minusDays(random.nextInt(DAYS_IN_YEAR));
                var expirationDate = adminDate.plusYears(CERT_EXPIRY_YEARS);
                var nextDueDate = expirationDate.minusDays(CERT_DUE_BEFORE_DAYS);

                var vetIndex = random.nextInt(context.getVeterinarianIds().size());
                var vaccineName = VACCINE_NAMES[random.nextInt(VACCINE_NAMES.length)];
                var certType =
                        vaccineName.contains("Rabies")
                                ? CertificateType.RABIES
                                : CertificateType.VACCINATION;

                var certificate =
                        VaccinationCertificate.builder()
                                .certificateNumber(certNum)
                                .certificateType(certType)
                                .patientId(patient.getId())
                                .patientName(patient.getName())
                                .patientSpecies(patient.getSpecies().name())
                                .patientBreed(patient.getBreed())
                                .microchipNumber(patient.getMicrochipNumber())
                                .clientId(client.getId())
                                .clientName(client.getFirstName() + " " + client.getLastName())
                                .vaccineName(vaccineName)
                                .vaccineManufacturer(
                                        MANUFACTURERS[random.nextInt(MANUFACTURERS.length)])
                                .batchNumber("BATCH-" + (1000 + random.nextInt(9000)))
                                .administrationDate(adminDate)
                                .expirationDate(expirationDate)
                                .nextDueDate(nextDueDate)
                                .veterinarianId(context.getVeterinarianIds().get(vetIndex))
                                .veterinarianName(context.getVeterinarianNames().get(vetIndex))
                                .veterinarianLicenseNumber("VET-" + (10000 + random.nextInt(90000)))
                                .isValid(!expirationDate.isBefore(today))
                                .build();
                certificate.setClinicId(context.getClinicId());
                entityManager.persist(certificate);
            }
        }
    }

    private void seedGdprConsents(EntityManager entityManager, SeedContext context) {
        var consentTypes = ConsentType.values();
        var random = context.getRandom();

        for (var client : context.getClients()) {
            var numConsents = 2 + random.nextInt(4);
            var usedTypes = new HashSet<ConsentType>();

            for (int i = 0; i < numConsents && usedTypes.size() < consentTypes.length; i++) {
                ConsentType consentType;
                do {
                    consentType = consentTypes[random.nextInt(consentTypes.length)];
                } while (usedTypes.contains(consentType));
                usedTypes.add(consentType);

                var requestedAt = LocalDateTime.now().minusDays(random.nextInt(DAYS_IN_YEAR));
                var statusInfo = determineConsentStatus(requestedAt, random);
                var consentText = getConsentText(consentType);

                var consent =
                        GdprConsent.builder()
                                .clientId(client.getId())
                                .consentType(consentType)
                                .status(statusInfo.status)
                                .requestedAt(requestedAt)
                                .grantedAt(statusInfo.grantedAt)
                                .revokedAt(statusInfo.revokedAt)
                                .expiresAt(statusInfo.expiresAt)
                                .consentText(consentText)
                                .consentVersion("1.0")
                                .ipAddress("192.168.1." + (1 + random.nextInt(254)))
                                .build();
                consent.setClinicId(context.getClinicId());
                entityManager.persist(consent);
            }
        }
    }

    private record ConsentStatusInfo(
            ConsentStatus status,
            LocalDateTime grantedAt,
            LocalDateTime revokedAt,
            LocalDate expiresAt) {}

    private ConsentStatusInfo determineConsentStatus(
            LocalDateTime requestedAt, java.util.Random random) {
        var statusChance = random.nextDouble();

        if (statusChance < GRANTED_PROBABILITY) {
            var grantedAt = requestedAt.plusMinutes(random.nextInt(60));
            var expiresAt = grantedAt.toLocalDate().plusYears(CONSENT_EXPIRY_YEARS);
            return new ConsentStatusInfo(ConsentStatus.GRANTED, grantedAt, null, expiresAt);
        } else if (statusChance < REVOKED_PROBABILITY) {
            var grantedAt = requestedAt.plusMinutes(random.nextInt(60));
            var revokedAt = grantedAt.plusDays(30 + random.nextInt(180));
            return new ConsentStatusInfo(ConsentStatus.REVOKED, grantedAt, revokedAt, null);
        } else if (statusChance < PENDING_PROBABILITY) {
            return new ConsentStatusInfo(ConsentStatus.PENDING, null, null, null);
        } else {
            var grantedAt = requestedAt.plusMinutes(random.nextInt(60));
            var expiresAt = LocalDate.now().minusDays(random.nextInt(30));
            return new ConsentStatusInfo(ConsentStatus.EXPIRED, grantedAt, null, expiresAt);
        }
    }

    private String getConsentText(ConsentType type) {
        return switch (type) {
            case DATA_PROCESSING -> CONSENT_TEXTS[0];
            case MARKETING -> CONSENT_TEXTS[1];
            case PHOTO_VIDEO -> CONSENT_TEXTS[2];
            case MEDICAL_PROCEDURES -> CONSENT_TEXTS[3];
            case ANESTHESIA -> CONSENT_TEXTS[4];
            case DATA_SHARING -> CONSENT_TEXTS[5];
            case RESEARCH -> CONSENT_TEXTS[6];
            default -> "I consent to the terms and conditions.";
        };
    }
}
