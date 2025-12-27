package com.vetclinic.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceItem;
import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.Payment;
import com.vetclinic.billing.domain.model.PaymentMethod;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.client.domain.model.Client;
import com.vetclinic.compliance.domain.model.CertificateType;
import com.vetclinic.compliance.domain.model.ConsentStatus;
import com.vetclinic.compliance.domain.model.ConsentType;
import com.vetclinic.compliance.domain.model.GdprConsent;
import com.vetclinic.compliance.domain.model.VaccinationCertificate;
import com.vetclinic.patient.domain.model.Gender;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;
import com.vetclinic.veterinarian.domain.model.Veterinarian;
import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitStatus;
import com.vetclinic.visit.domain.model.VisitType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    /**
     * Hardcoded clinic ID that matches Keycloak user attributes. This ensures test users can see
     * the seed data.
     */
    private static final UUID DEFAULT_CLINIC_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final EntityManager entityManager;
    private final Random random = new Random(42);

    private UUID clinicId;
    private final List<UUID> clientIds = new ArrayList<>();
    private final List<UUID> patientIds = new ArrayList<>();
    private final List<Patient> patients = new ArrayList<>();
    private final List<UUID> veterinarianIds = new ArrayList<>();
    private final List<String> veterinarianNames = new ArrayList<>();
    private final List<Visit> completedVisits = new ArrayList<>();
    private final List<Client> clients = new ArrayList<>();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting data initialization...");

        // Check if data already exists
        Long clinicCount =
                entityManager
                        .createQuery("SELECT COUNT(c) FROM VeterinaryClinic c", Long.class)
                        .getSingleResult();
        if (clinicCount > 0) {
            log.info("Data already exists, clearing and reinitializing...");
            clearAllData();
        }

        initializeClinic();
        initializeVeterinarians();
        initializeClients();
        initializePatients();
        initializePriceListItems();
        initializeVisits();
        initializeInvoicesAndPayments();
        initializeCertificates();
        initializeGdprConsents();

        log.info("Data initialization completed successfully!");
        log.info(
                "Created: 1 clinic, {} veterinarians, {} clients, {} patients, {} visits",
                veterinarianIds.size(),
                clientIds.size(),
                patientIds.size(),
                countVisits());
        log.info(
                "Created: {} invoices, {} payments, {} certificates, {} GDPR consents",
                countInvoices(),
                countPayments(),
                countCertificates(),
                countConsents());
    }

    private void clearAllData() {
        entityManager.createQuery("DELETE FROM Payment").executeUpdate();
        entityManager.createQuery("DELETE FROM Invoice").executeUpdate();
        entityManager.createQuery("DELETE FROM VaccinationCertificate").executeUpdate();
        entityManager.createQuery("DELETE FROM GdprConsent").executeUpdate();
        entityManager.createQuery("DELETE FROM Visit").executeUpdate();
        entityManager.createQuery("DELETE FROM Patient").executeUpdate();
        entityManager.createQuery("DELETE FROM Client").executeUpdate();
        entityManager.createQuery("DELETE FROM Veterinarian").executeUpdate();
        entityManager.createQuery("DELETE FROM PriceListItem").executeUpdate();
        entityManager.createQuery("DELETE FROM AuditLog").executeUpdate();
        entityManager.createQuery("DELETE FROM VeterinaryClinic").executeUpdate();
        entityManager.flush();
        entityManager.clear(); // Detach all entities to allow fresh inserts
    }

    private Long countVisits() {
        return entityManager
                .createQuery("SELECT COUNT(v) FROM Visit v", Long.class)
                .getSingleResult();
    }

    private Long countInvoices() {
        return entityManager
                .createQuery("SELECT COUNT(i) FROM Invoice i", Long.class)
                .getSingleResult();
    }

    private Long countPayments() {
        return entityManager
                .createQuery("SELECT COUNT(p) FROM Payment p", Long.class)
                .getSingleResult();
    }

    private Long countCertificates() {
        return entityManager
                .createQuery("SELECT COUNT(c) FROM VaccinationCertificate c", Long.class)
                .getSingleResult();
    }

    private Long countConsents() {
        return entityManager
                .createQuery("SELECT COUNT(c) FROM GdprConsent c", Long.class)
                .getSingleResult();
    }

    private void initializeClinic() {
        // Use native SQL to insert clinic with specific ID (bypasses JPA @GeneratedValue)
        entityManager
                .createNativeQuery(
                        """
                INSERT INTO veterinary_clinics (id, name, slug, email, phone, address, city, postal_code, active, created_at)
                VALUES (:id, :name, :slug, :email, :phone, :address, :city, :postalCode, :active, CURRENT_TIMESTAMP)
                """)
                .setParameter("id", DEFAULT_CLINIC_ID)
                .setParameter("name", "Happy Paws Veterinary Clinic")
                .setParameter("slug", "happy-paws")
                .setParameter("email", "contact@happypaws.vet")
                .setParameter("phone", "+48 123 456 789")
                .setParameter("address", "ul. Zwierzęca 15")
                .setParameter("city", "Warszawa")
                .setParameter("postalCode", "00-001")
                .setParameter("active", true)
                .executeUpdate();
        entityManager.flush();
        clinicId = DEFAULT_CLINIC_ID;
        log.info("Created clinic: Happy Paws Veterinary Clinic (ID: {})", clinicId);
    }

    private void initializeVeterinarians() {
        String[][] vets = {
            {"Anna", "Kowalska", "anna.kowalska@happypaws.vet", "Surgery", "#4CAF50", "VET-001"},
            {
                "Piotr",
                "Nowak",
                "piotr.nowak@happypaws.vet",
                "Internal Medicine",
                "#2196F3",
                "VET-002"
            },
            {
                "Maria",
                "Wiśniewska",
                "maria.wisniewska@happypaws.vet",
                "Dermatology",
                "#9C27B0",
                "VET-003"
            },
            {
                "Jan",
                "Kowalczyk",
                "jan.kowalczyk@happypaws.vet",
                "Orthopedics",
                "#FF9800",
                "VET-004"
            },
            {
                "Katarzyna",
                "Lewandowska",
                "katarzyna.lewandowska@happypaws.vet",
                "Cardiology",
                "#E91E63",
                "VET-005"
            },
            {
                "Tomasz",
                "Zieliński",
                "tomasz.zielinski@happypaws.vet",
                "Ophthalmology",
                "#00BCD4",
                "VET-006"
            }
        };

        for (String[] vet : vets) {
            Veterinarian veterinarian =
                    Veterinarian.builder()
                            .firstName(vet[0])
                            .lastName(vet[1])
                            .email(vet[2])
                            .phone("+48 " + (500000000 + random.nextInt(99999999)))
                            .specialization(vet[3])
                            .colorCode(vet[4])
                            .licenseNumber(vet[5])
                            .active(true)
                            .build();
            veterinarian.setClinicId(clinicId);
            entityManager.persist(veterinarian);
            veterinarianIds.add(veterinarian.getId());
            veterinarianNames.add(veterinarian.getFullName());
        }
        log.info("Created {} veterinarians", veterinarianIds.size());
    }

    private void initializeClients() {
        String[][] clientData = {
            {
                "Adam",
                "Malinowski",
                "adam.malinowski@email.pl",
                "+48 601 234 567",
                "ul. Kwiatowa 5",
                "Warszawa",
                "00-100"
            },
            {
                "Barbara",
                "Kaczmarek",
                "b.kaczmarek@gmail.com",
                "+48 602 345 678",
                "ul. Słoneczna 12",
                "Kraków",
                "30-001"
            },
            {
                "Cezary",
                "Wójcik",
                "cezary.wojcik@wp.pl",
                "+48 603 456 789",
                "ul. Leśna 8",
                "Gdańsk",
                "80-001"
            },
            {
                "Dorota",
                "Kamińska",
                "dorota.k@onet.pl",
                "+48 604 567 890",
                "ul. Morska 23",
                "Sopot",
                "81-701"
            },
            {
                "Edward",
                "Szymański",
                "e.szymanski@firma.pl",
                "+48 605 678 901",
                "ul. Górska 45",
                "Zakopane",
                "34-500"
            },
            {
                "Franciszka",
                "Woźniak",
                "fwoźniak@email.com",
                "+48 606 789 012",
                "ul. Polna 67",
                "Poznań",
                "60-001"
            },
            {
                "Grzegorz",
                "Dąbrowski",
                "g.dabrowski@mail.pl",
                "+48 607 890 123",
                "ul. Łąkowa 89",
                "Wrocław",
                "50-001"
            },
            {
                "Helena",
                "Kozłowska",
                "helena.kozl@interia.pl",
                "+48 608 901 234",
                "ul. Rzeczna 12",
                "Łódź",
                "90-001"
            },
            {
                "Igor",
                "Jankowski",
                "igor.jan@gmail.com",
                "+48 609 012 345",
                "ul. Parkowa 34",
                "Katowice",
                "40-001"
            },
            {
                "Joanna",
                "Mazur",
                "j.mazur@outlook.com",
                "+48 610 123 456",
                "ul. Ogrodowa 56",
                "Lublin",
                "20-001"
            },
            {
                "Krzysztof",
                "Krawczyk",
                "k.krawczyk@email.pl",
                "+48 611 234 567",
                "ul. Zielona 78",
                "Szczecin",
                "70-001"
            },
            {
                "Lucyna",
                "Piotrowska",
                "lucyna.p@wp.pl",
                "+48 612 345 678",
                "ul. Wesoła 90",
                "Bydgoszcz",
                "85-001"
            },
            {
                "Marek",
                "Grabowski",
                "m.grabowski@firma.com",
                "+48 613 456 789",
                "ul. Cicha 11",
                "Białystok",
                "15-001"
            },
            {
                "Natalia",
                "Pawlak",
                "n.pawlak@gmail.com",
                "+48 614 567 890",
                "ul. Spokojna 22",
                "Gdynia",
                "81-001"
            },
            {
                "Olga",
                "Michalska",
                "olga.m@onet.pl",
                "+48 615 678 901",
                "ul. Radosna 33",
                "Częstochowa",
                "42-200"
            },
            {
                "Paweł",
                "Zając",
                "pawel.zajac@mail.com",
                "+48 616 789 012",
                "ul. Słowicza 44",
                "Radom",
                "26-600"
            },
            {
                "Renata",
                "Król",
                "r.krol@interia.pl",
                "+48 617 890 123",
                "ul. Jaskółcza 55",
                "Toruń",
                "87-100"
            },
            {
                "Stefan",
                "Wieczorek",
                "s.wieczorek@email.pl",
                "+48 618 901 234",
                "ul. Ptasia 66",
                "Kielce",
                "25-001"
            }
        };

        for (String[] c : clientData) {
            Client client =
                    Client.builder()
                            .firstName(c[0])
                            .lastName(c[1])
                            .email(c[2])
                            .phone(c[3])
                            .address(c[4])
                            .city(c[5])
                            .postalCode(c[6])
                            .notes(
                                    random.nextDouble() > 0.7
                                            ? "Regular customer, always on time"
                                            : null)
                            .build();
            client.setClinicId(clinicId);
            entityManager.persist(client);
            clientIds.add(client.getId());
            clients.add(client);
        }
        log.info("Created {} clients", clientIds.size());
    }

    private void initializePatients() {
        // Dogs
        createPatient(
                "Burek",
                Species.DOG,
                "German Shepherd",
                Gender.MALE,
                35.5,
                "Black and tan",
                true,
                5,
                0);
        createPatient(
                "Luna", Species.DOG, "Golden Retriever", Gender.FEMALE, 28.0, "Golden", true, 3, 1);
        createPatient("Max", Species.DOG, "Labrador", Gender.MALE, 32.0, "Black", true, 7, 2);
        createPatient("Bella", Species.DOG, "Beagle", Gender.FEMALE, 12.5, "Tricolor", false, 2, 3);
        createPatient("Rocky", Species.DOG, "Boxer", Gender.MALE, 30.0, "Fawn", true, 4, 4);
        createPatient(
                "Mila", Species.DOG, "French Bulldog", Gender.FEMALE, 11.0, "Brindle", true, 1, 5);
        createPatient(
                "Bruno",
                Species.DOG,
                "Rottweiler",
                Gender.MALE,
                45.0,
                "Black and mahogany",
                true,
                6,
                6);
        createPatient(
                "Kira", Species.DOG, "Husky", Gender.FEMALE, 22.0, "Gray and white", false, 3, 7);
        createPatient(
                "Rex", Species.DOG, "German Shepherd", Gender.MALE, 38.0, "Sable", true, 8, 8);
        createPatient("Nala", Species.DOG, "Poodle", Gender.FEMALE, 8.0, "White", true, 4, 9);

        // Cats
        createPatient("Mruczek", Species.CAT, "Persian", Gender.MALE, 5.5, "White", true, 6, 10);
        createPatient(
                "Kicia", Species.CAT, "Siamese", Gender.FEMALE, 4.0, "Seal point", true, 3, 11);
        createPatient(
                "Filemon", Species.CAT, "Maine Coon", Gender.MALE, 8.5, "Brown tabby", true, 5, 12);
        createPatient(
                "Puszek", Species.CAT, "British Shorthair", Gender.MALE, 6.0, "Blue", true, 2, 13);
        createPatient(
                "Misia", Species.CAT, "Ragdoll", Gender.FEMALE, 5.0, "Blue bicolor", false, 1, 14);
        createPatient("Tygrys", Species.CAT, "Bengal", Gender.MALE, 5.5, "Spotted", true, 4, 15);
        createPatient("Cleo", Species.CAT, "Abyssinian", Gender.FEMALE, 3.5, "Ruddy", true, 2, 16);

        // Other animals
        createPatient(
                "Tweetie", Species.BIRD, "Canary", Gender.FEMALE, 0.025, "Yellow", false, 2, 0);
        createPatient("Coco", Species.BIRD, "Cockatiel", Gender.MALE, 0.09, "Gray", false, 5, 1);
        createPatient(
                "Bunny",
                Species.RABBIT,
                "Holland Lop",
                Gender.FEMALE,
                2.0,
                "White and brown",
                false,
                1,
                2);
        createPatient("Flopsy", Species.RABBIT, "Mini Rex", Gender.MALE, 1.8, "Black", true, 3, 3);
        createPatient(
                "Chomik", Species.HAMSTER, "Syrian", Gender.MALE, 0.15, "Golden", false, 1, 4);
        createPatient(
                "Żółwik",
                Species.REPTILE,
                "Red-eared slider",
                Gender.MALE,
                0.8,
                "Green with red",
                false,
                15,
                5);
        createPatient(
                "Nemo",
                Species.FISH,
                "Clownfish",
                Gender.MALE,
                0.02,
                "Orange and white",
                false,
                2,
                6);

        log.info("Created {} patients", patientIds.size());
    }

    private void createPatient(
            String name,
            Species species,
            String breed,
            Gender gender,
            double weight,
            String color,
            boolean neutered,
            int ageYears,
            int ownerIndex) {
        Patient patient =
                Patient.builder()
                        .name(name)
                        .species(species)
                        .breed(breed)
                        .gender(gender)
                        .weight(weight)
                        .color(color)
                        .neutered(neutered)
                        .dateOfBirth(
                                LocalDate.now().minusYears(ageYears).minusDays(random.nextInt(365)))
                        .ownerId(clientIds.get(ownerIndex % clientIds.size()))
                        .microchipNumber(
                                species == Species.DOG || species == Species.CAT
                                        ? "PL"
                                                + String.format(
                                                        "%013d", random.nextLong(9999999999999L))
                                        : null)
                        .labels(generateLabels(species, ageYears))
                        .build();
        patient.setClinicId(clinicId);
        entityManager.persist(patient);
        patientIds.add(patient.getId());
        patients.add(patient);
    }

    private Set<PatientLabel> generateLabels(Species species, int ageYears) {
        Set<PatientLabel> labels = new java.util.HashSet<>();
        if (ageYears >= 7 && (species == Species.DOG || species == Species.CAT)) {
            labels.add(PatientLabel.SENIOR);
        }
        if (random.nextDouble() > 0.9) {
            labels.add(PatientLabel.VIP);
        }
        if (random.nextDouble() > 0.85) {
            labels.add(PatientLabel.AGGRESSIVE);
        }
        if (random.nextDouble() > 0.9) {
            labels.add(PatientLabel.ALLERGIC);
        }
        if (random.nextDouble() > 0.85) {
            labels.add(PatientLabel.CHRONIC);
        }
        return labels;
    }

    private void initializePriceListItems() {
        // Consultations
        createPriceListItem(
                "General Consultation",
                "Standard veterinary examination",
                ItemCategory.CONSULTATION,
                80,
                150,
                "visit",
                "CONS-001");
        createPriceListItem(
                "Specialist Consultation",
                "Consultation with specialist veterinarian",
                ItemCategory.CONSULTATION,
                120,
                250,
                "visit",
                "CONS-002");
        createPriceListItem(
                "Emergency Consultation",
                "After-hours emergency examination",
                ItemCategory.CONSULTATION,
                150,
                350,
                "visit",
                "CONS-003");
        createPriceListItem(
                "Follow-up Consultation",
                "Follow-up examination",
                ItemCategory.CONSULTATION,
                50,
                100,
                "visit",
                "CONS-004");

        // Vaccinations
        createPriceListItem(
                "Dog Vaccination - DHPP",
                "Distemper, Hepatitis, Parainfluenza, Parvovirus",
                ItemCategory.VACCINATION,
                40,
                120,
                "dose",
                "VAC-001");
        createPriceListItem(
                "Dog Vaccination - Rabies",
                "Rabies vaccine for dogs",
                ItemCategory.VACCINATION,
                35,
                100,
                "dose",
                "VAC-002");
        createPriceListItem(
                "Cat Vaccination - FVRCP",
                "Feline viral rhinotracheitis, calicivirus, panleukopenia",
                ItemCategory.VACCINATION,
                38,
                110,
                "dose",
                "VAC-003");
        createPriceListItem(
                "Cat Vaccination - Rabies",
                "Rabies vaccine for cats",
                ItemCategory.VACCINATION,
                35,
                100,
                "dose",
                "VAC-004");
        createPriceListItem(
                "Kennel Cough Vaccine",
                "Bordetella bronchiseptica vaccination",
                ItemCategory.VACCINATION,
                30,
                80,
                "dose",
                "VAC-005");

        // Procedures
        createPriceListItem(
                "Neutering - Male Dog",
                "Castration surgery for male dogs",
                ItemCategory.PROCEDURE,
                200,
                450,
                "surgery",
                "PROC-001");
        createPriceListItem(
                "Spaying - Female Dog",
                "Ovariohysterectomy for female dogs",
                ItemCategory.PROCEDURE,
                300,
                650,
                "surgery",
                "PROC-002");
        createPriceListItem(
                "Neutering - Male Cat",
                "Castration surgery for male cats",
                ItemCategory.PROCEDURE,
                100,
                250,
                "surgery",
                "PROC-003");
        createPriceListItem(
                "Spaying - Female Cat",
                "Ovariohysterectomy for female cats",
                ItemCategory.PROCEDURE,
                150,
                350,
                "surgery",
                "PROC-004");
        createPriceListItem(
                "Dental Cleaning",
                "Professional teeth cleaning and polishing",
                ItemCategory.PROCEDURE,
                150,
                400,
                "procedure",
                "PROC-005");
        createPriceListItem(
                "Tooth Extraction",
                "Single tooth extraction",
                ItemCategory.PROCEDURE,
                50,
                150,
                "tooth",
                "PROC-006");
        createPriceListItem(
                "Microchip Implantation",
                "Pet microchip insertion",
                ItemCategory.PROCEDURE,
                30,
                100,
                "chip",
                "PROC-007");
        createPriceListItem(
                "Nail Trimming",
                "Professional nail clipping",
                ItemCategory.PROCEDURE,
                10,
                40,
                "procedure",
                "PROC-008");
        createPriceListItem(
                "Ear Cleaning",
                "Professional ear cleaning",
                ItemCategory.PROCEDURE,
                15,
                50,
                "procedure",
                "PROC-009");

        // Lab Tests
        createPriceListItem(
                "Blood Test - Complete",
                "Complete blood count and chemistry panel",
                ItemCategory.LAB_TEST,
                80,
                200,
                "test",
                "LAB-001");
        createPriceListItem(
                "Blood Test - Basic",
                "Basic blood chemistry",
                ItemCategory.LAB_TEST,
                40,
                100,
                "test",
                "LAB-002");
        createPriceListItem(
                "Urinalysis",
                "Complete urine analysis",
                ItemCategory.LAB_TEST,
                30,
                80,
                "test",
                "LAB-003");
        createPriceListItem(
                "Fecal Examination",
                "Parasite screening",
                ItemCategory.LAB_TEST,
                25,
                60,
                "test",
                "LAB-004");
        createPriceListItem(
                "X-Ray - Single View",
                "Single radiograph",
                ItemCategory.LAB_TEST,
                60,
                150,
                "image",
                "LAB-005");
        createPriceListItem(
                "X-Ray - Multiple Views",
                "Multiple radiograph views",
                ItemCategory.LAB_TEST,
                100,
                250,
                "series",
                "LAB-006");
        createPriceListItem(
                "Ultrasound",
                "Diagnostic ultrasound examination",
                ItemCategory.LAB_TEST,
                120,
                300,
                "exam",
                "LAB-007");

        // Medications
        createPriceListItem(
                "Antibiotic - Amoxicillin",
                "Broad spectrum antibiotic",
                ItemCategory.MEDICATION,
                15,
                45,
                "course",
                "MED-001");
        createPriceListItem(
                "Anti-inflammatory - Meloxicam",
                "Non-steroidal anti-inflammatory",
                ItemCategory.MEDICATION,
                20,
                55,
                "course",
                "MED-002");
        createPriceListItem(
                "Flea Treatment - Frontline",
                "Monthly flea and tick prevention",
                ItemCategory.MEDICATION,
                25,
                60,
                "dose",
                "MED-003");
        createPriceListItem(
                "Dewormer - Drontal",
                "Broad spectrum dewormer",
                ItemCategory.MEDICATION,
                15,
                40,
                "tablet",
                "MED-004");
        createPriceListItem(
                "Eye Drops - Antibiotic",
                "Ophthalmic antibiotic solution",
                ItemCategory.MEDICATION,
                18,
                50,
                "bottle",
                "MED-005");
        createPriceListItem(
                "Ear Drops - Otitis Treatment",
                "Ear infection treatment",
                ItemCategory.MEDICATION,
                22,
                60,
                "bottle",
                "MED-006");
        createPriceListItem(
                "Pain Relief - Tramadol",
                "Prescription pain medication",
                ItemCategory.MEDICATION,
                25,
                70,
                "course",
                "MED-007");

        // Products
        createPriceListItem(
                "Elizabethan Collar - Small",
                "Recovery cone small size",
                ItemCategory.PRODUCT,
                8,
                25,
                "piece",
                "PROD-001");
        createPriceListItem(
                "Elizabethan Collar - Medium",
                "Recovery cone medium size",
                ItemCategory.PRODUCT,
                10,
                30,
                "piece",
                "PROD-002");
        createPriceListItem(
                "Elizabethan Collar - Large",
                "Recovery cone large size",
                ItemCategory.PRODUCT,
                12,
                35,
                "piece",
                "PROD-003");
        createPriceListItem(
                "Bandage - Elastic",
                "Self-adhesive elastic bandage",
                ItemCategory.PRODUCT,
                5,
                15,
                "roll",
                "PROD-004");
        createPriceListItem(
                "Surgical Sutures",
                "Absorbable surgical sutures",
                ItemCategory.PRODUCT,
                20,
                50,
                "pack",
                "PROD-005");

        log.info("Created price list items");
    }

    private void createPriceListItem(
            String name,
            String description,
            ItemCategory category,
            double cost,
            double sell,
            String unit,
            String code) {
        // Services don't need stock tracking
        var isService =
                category == ItemCategory.CONSULTATION
                        || category == ItemCategory.PROCEDURE
                        || category == ItemCategory.LAB_TEST;

        // Generate realistic stock quantities for products
        Integer stockQty = null;
        Integer reorderPt = null;
        if (!isService) {
            reorderPt = 10 + random.nextInt(15); // 10-25
            // Mix of stock levels: some good, some low, some out
            var stockLevel = random.nextDouble();
            if (stockLevel < 0.1) {
                stockQty = 0; // Out of stock (10%)
            } else if (stockLevel < 0.25) {
                stockQty = random.nextInt(reorderPt); // Low stock (15%)
            } else {
                stockQty = reorderPt + 10 + random.nextInt(100); // In stock (75%)
            }
        }

        PriceListItem item =
                PriceListItem.builder()
                        .name(name)
                        .description(description)
                        .category(category)
                        .costPrice(BigDecimal.valueOf(cost))
                        .sellPrice(BigDecimal.valueOf(sell))
                        .unit(unit)
                        .code(code)
                        .stockQuantity(stockQty)
                        .reorderPoint(reorderPt)
                        .active(true)
                        .build();
        item.setClinicId(clinicId);
        entityManager.persist(item);
    }

    private void initializeVisits() {
        LocalDate today = LocalDate.now();
        String[] visitReasons = {
            "Annual checkup",
            "Vaccination",
            "Skin problem",
            "Limping",
            "Vomiting",
            "Diarrhea",
            "Ear infection",
            "Eye discharge",
            "Dental issue",
            "Weight management",
            "Allergy symptoms",
            "Follow-up examination",
            "Coughing",
            "Lethargy",
            "Not eating",
            "Wound care",
            "Post-surgery checkup",
            "Senior wellness exam"
        };

        // Past completed visits (last 60 days) - around 80 visits
        for (int dayOffset = 60; dayOffset >= 1; dayOffset--) {
            LocalDate visitDate = today.minusDays(dayOffset);
            if (visitDate.getDayOfWeek().getValue() <= 5) { // Weekdays only
                int visitsPerDay = 2 + random.nextInt(4); // 2-5 visits per day
                for (int i = 0; i < visitsPerDay; i++) {
                    createVisit(
                            visitDate, 8 + random.nextInt(9), VisitStatus.COMPLETED, visitReasons);
                }
            }
        }

        // Today's visits - mix of statuses
        if (today.getDayOfWeek().getValue() <= 5) {
            int currentHour = LocalTime.now().getHour();

            // Earlier today - completed
            for (int hour = 8; hour < Math.min(currentHour, 12); hour++) {
                if (random.nextDouble() > 0.3) {
                    createVisit(today, hour, VisitStatus.COMPLETED, visitReasons);
                }
            }

            // Current time slot - in progress
            if (currentHour >= 8 && currentHour <= 17) {
                createVisit(today, currentHour, VisitStatus.IN_PROGRESS, visitReasons);
            }

            // Rest of today - scheduled
            for (int hour = Math.max(currentHour + 1, 8); hour <= 17; hour++) {
                if (random.nextDouble() > 0.4) {
                    createVisit(today, hour, VisitStatus.SCHEDULED, visitReasons);
                }
            }
        }

        // Future scheduled visits (next 14 days) - around 30 visits
        for (int dayOffset = 1; dayOffset <= 14; dayOffset++) {
            LocalDate visitDate = today.plusDays(dayOffset);
            if (visitDate.getDayOfWeek().getValue() <= 5) { // Weekdays only
                int visitsPerDay = 1 + random.nextInt(4); // 1-4 visits per day
                for (int i = 0; i < visitsPerDay; i++) {
                    createVisit(
                            visitDate, 8 + random.nextInt(10), VisitStatus.SCHEDULED, visitReasons);
                }
            }
        }

        // Some cancelled visits
        for (int i = 0; i < 5; i++) {
            LocalDate visitDate = today.minusDays(random.nextInt(30));
            if (visitDate.getDayOfWeek().getValue() <= 5) {
                createVisit(visitDate, 8 + random.nextInt(10), VisitStatus.CANCELLED, visitReasons);
            }
        }
    }

    private void createVisit(LocalDate date, int hour, VisitStatus status, String[] reasons) {
        int patientIndex = random.nextInt(patients.size());
        Patient patient = patients.get(patientIndex);
        int vetIndex = random.nextInt(veterinarianIds.size());

        int[] durations = {15, 30, 30, 30, 45, 60};
        int duration = durations[random.nextInt(durations.length)];

        // Assign random visit types with weighted distribution (more common types appear more
        // often)
        VisitType[] visitTypes = {
            VisitType.CONSULTATION,
            VisitType.CONSULTATION,
            VisitType.CONSULTATION, // more common
            VisitType.CHECKUP,
            VisitType.CHECKUP, // more common
            VisitType.VACCINATION,
            VisitType.VACCINATION,
            VisitType.LAB_WORK,
            VisitType.FOLLOW_UP,
            VisitType.ULTRASOUND,
            VisitType.CARDIOLOGY,
            VisitType.DENTAL,
            VisitType.SURGERY,
            VisitType.GROOMING,
            VisitType.EMERGENCY
        };
        VisitType visitType = visitTypes[random.nextInt(visitTypes.length)];

        // Set appropriate reason based on visit type
        String reason;
        switch (visitType) {
            case VACCINATION -> reason = getVaccinationReason();
            case LAB_WORK -> reason = getLabWorkReason();
            case ULTRASOUND -> reason = getUltrasoundReason();
            case CARDIOLOGY -> reason = getCardiologyReason();
            case DENTAL -> reason = getDentalReason();
            case SURGERY -> reason = getSurgeryReason();
            case GROOMING -> reason = getGroomingReason();
            case EMERGENCY -> reason = getEmergencyReason();
            case FOLLOW_UP -> reason = "Follow-up examination";
            case CHECKUP -> reason = "Annual checkup";
            case CONSULTATION -> reason = reasons[random.nextInt(reasons.length)];
            default -> reason = reasons[random.nextInt(reasons.length)];
        }

        Visit visit =
                Visit.builder()
                        .patientId(patient.getId())
                        .clientId(patient.getOwnerId())
                        .veterinarianId(veterinarianIds.get(vetIndex))
                        .veterinarianName(veterinarianNames.get(vetIndex))
                        .visitDate(LocalDateTime.of(date, LocalTime.of(hour, 0)))
                        .durationMinutes(duration)
                        .status(status)
                        .visitType(visitType)
                        .reason(reason)
                        .build();

        // Add details for completed visits
        if (status == VisitStatus.COMPLETED) {
            visit.setWeight(
                    patient.getWeight() != null
                            ? Math.max(0.01, patient.getWeight() + (random.nextDouble() - 0.5))
                            : null);
            visit.setTemperature(38.0 + random.nextDouble() * 1.5);
            visit.setInterview(
                    "Patient brought in for "
                            + reason.toLowerCase()
                            + ". Owner reports symptoms started "
                            + (1 + random.nextInt(7))
                            + " days ago.");
            visit.setExamination(
                    "Physical examination performed. Vital signs within normal limits. "
                            + generateExaminationNotes());
            visit.setDiagnosis(generateDiagnosis(reason));
            visit.setTreatment(generateTreatment(reason));
            visit.setRecommendations(generateRecommendations());
            visit.setNotes("Visit completed successfully.");
            if (random.nextDouble() > 0.7) {
                visit.setNextVisitDate(date.plusDays(7 + random.nextInt(21)));
            }
        }

        visit.setClinicId(clinicId);
        entityManager.persist(visit);

        if (status == VisitStatus.COMPLETED) {
            completedVisits.add(visit);
        }
    }

    private String generateExaminationNotes() {
        String[] notes = {
            "No abnormalities detected.",
            "Mild inflammation observed.",
            "Patient cooperative during examination.",
            "Heart and lungs clear.",
            "Slight tenderness in affected area.",
            "Lymph nodes normal size.",
            "Good body condition score.",
            "Coat and skin in good condition."
        };
        return notes[random.nextInt(notes.length)];
    }

    private String generateDiagnosis(String reason) {
        if (reason.contains("Vaccination")) return "Healthy patient, cleared for vaccination.";
        if (reason.contains("checkup"))
            return "Overall good health. Minor findings discussed with owner.";
        if (reason.contains("Skin"))
            return "Suspected allergic dermatitis. Further testing recommended.";
        if (reason.contains("Limping")) return "Mild soft tissue injury. No fractures detected.";
        if (reason.contains("Ear")) return "Otitis externa. Bacterial infection suspected.";
        if (reason.contains("Dental"))
            return "Moderate tartar buildup. Professional cleaning recommended.";
        return "Condition assessed. Treatment plan established.";
    }

    private String generateTreatment(String reason) {
        if (reason.contains("Vaccination"))
            return "Administered scheduled vaccination. No adverse reactions.";
        if (reason.contains("Skin")) return "Prescribed antihistamines and medicated shampoo.";
        if (reason.contains("Limping"))
            return "Rest recommended. Anti-inflammatory medication prescribed.";
        if (reason.contains("Ear"))
            return "Ear cleaning performed. Antibiotic ear drops prescribed.";
        if (reason.contains("Dental"))
            return "Dental cleaning scheduled. Pre-operative blood work ordered.";
        return "Appropriate treatment administered.";
    }

    private String generateRecommendations() {
        String[] recs = {
            "Continue current diet and exercise routine.",
            "Return in 2 weeks for follow-up.",
            "Monitor for any changes in behavior or appetite.",
            "Keep wound clean and dry.",
            "Complete full course of medication.",
            "Restrict activity for 1 week.",
            "Schedule dental cleaning appointment.",
            "Annual vaccination due in 12 months."
        };
        return recs[random.nextInt(recs.length)];
    }

    private String getVaccinationReason() {
        String[] reasons = {
            "Annual vaccination - DHPP",
            "Rabies vaccination",
            "Annual vaccination - FVRCP",
            "Kennel cough vaccine",
            "Booster vaccination",
            "Puppy vaccination series",
            "Kitten vaccination series"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String getLabWorkReason() {
        String[] reasons = {
            "Blood work panel",
            "Complete blood count",
            "Pre-surgical blood work",
            "Senior wellness blood test",
            "Kidney function test",
            "Liver function test",
            "Thyroid panel",
            "Urinalysis"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String getUltrasoundReason() {
        String[] reasons = {
            "Abdominal ultrasound",
            "Pregnancy ultrasound",
            "Cardiac ultrasound",
            "Bladder ultrasound",
            "Liver ultrasound examination",
            "Kidney ultrasound"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String getCardiologyReason() {
        String[] reasons = {
            "Heart examination",
            "Heart murmur evaluation",
            "Cardiac ultrasound",
            "ECG examination",
            "Arrhythmia check",
            "Heart disease follow-up"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String getDentalReason() {
        String[] reasons = {
            "Dental cleaning",
            "Tooth extraction",
            "Dental examination",
            "Oral surgery",
            "Gum disease treatment",
            "Broken tooth repair"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String getSurgeryReason() {
        String[] reasons = {
            "Spaying surgery",
            "Neutering surgery",
            "Tumor removal",
            "Laceration repair",
            "Orthopedic surgery",
            "Foreign body removal",
            "Mass excision"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String getGroomingReason() {
        String[] reasons = {
            "Full grooming service",
            "Nail trimming",
            "Ear cleaning",
            "Coat trimming",
            "Bath and brush",
            "Sanitary trim"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String getEmergencyReason() {
        String[] reasons = {
            "Emergency - hit by car",
            "Emergency - difficulty breathing",
            "Emergency - seizure",
            "Emergency - toxic ingestion",
            "Emergency - severe vomiting",
            "Emergency - trauma",
            "Emergency - bloat",
            "Emergency - collapse"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private void initializeInvoicesAndPayments() {
        var invoiceNumber = 1;
        var paymentMethods = PaymentMethod.values();

        // Create invoices for ~60% of completed visits
        for (var visit : completedVisits) {
            if (random.nextDouble() > 0.4) {
                continue;
            }

            var invoiceNum =
                    String.format("INV-%d-%04d", LocalDate.now().getYear(), invoiceNumber++);
            var visitDate = visit.getVisitDate().toLocalDate();

            // Generate random items for the invoice
            var items = new ArrayList<InvoiceItem>();
            var subtotal = BigDecimal.ZERO;

            // Consultation fee
            var consultationFee = BigDecimal.valueOf(100 + random.nextInt(150));
            items.add(
                    InvoiceItem.builder()
                            .name("Consultation")
                            .description(visit.getReason())
                            .quantity(1)
                            .unitPrice(consultationFee)
                            .total(consultationFee)
                            .build());
            subtotal = subtotal.add(consultationFee);

            // Random additional items
            if (random.nextDouble() > 0.5) {
                var medicationFee = BigDecimal.valueOf(30 + random.nextInt(70));
                items.add(
                        InvoiceItem.builder()
                                .name("Medication")
                                .description("Prescribed medication")
                                .quantity(1 + random.nextInt(2))
                                .unitPrice(medicationFee)
                                .total(
                                        medicationFee.multiply(
                                                BigDecimal.valueOf(1 + random.nextInt(2))))
                                .build());
                subtotal = subtotal.add(items.get(items.size() - 1).getTotal());
            }

            var taxRate = BigDecimal.valueOf(23);
            var taxAmount =
                    subtotal.multiply(taxRate)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            var totalAmount = subtotal.add(taxAmount);

            // Determine status based on payment
            InvoiceStatus status;
            var paidAmount = BigDecimal.ZERO;

            var paymentChance = random.nextDouble();
            if (paymentChance < 0.6) {
                // Fully paid
                status = InvoiceStatus.PAID;
                paidAmount = totalAmount;
            } else if (paymentChance < 0.75) {
                // Partially paid
                status = InvoiceStatus.PARTIALLY_PAID;
                paidAmount =
                        totalAmount.multiply(BigDecimal.valueOf(0.3 + random.nextDouble() * 0.4));
            } else if (paymentChance < 0.9) {
                // Issued but not paid
                status = InvoiceStatus.ISSUED;
            } else {
                // Overdue (older invoices)
                status =
                        visitDate.isBefore(LocalDate.now().minusDays(30))
                                ? InvoiceStatus.OVERDUE
                                : InvoiceStatus.ISSUED;
            }

            var invoice =
                    Invoice.builder()
                            .invoiceNumber(invoiceNum)
                            .clientId(visit.getClientId())
                            .patientId(visit.getPatientId())
                            .visitId(visit.getId())
                            .issueDate(visitDate)
                            .dueDate(visitDate.plusDays(14))
                            .status(status)
                            .items(items)
                            .subtotal(subtotal)
                            .taxRate(taxRate)
                            .taxAmount(taxAmount)
                            .totalAmount(totalAmount)
                            .paidAmount(paidAmount)
                            .build();
            invoice.setClinicId(clinicId);
            entityManager.persist(invoice);

            // Create payments for paid/partially paid invoices
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                var payment =
                        Payment.builder()
                                .invoiceId(invoice.getId())
                                .amount(paidAmount)
                                .paymentMethod(
                                        paymentMethods[random.nextInt(paymentMethods.length)])
                                .paymentDate(
                                        visitDate.atTime(
                                                10 + random.nextInt(8), random.nextInt(60)))
                                .transactionReference(
                                        paymentMethods[random.nextInt(paymentMethods.length)]
                                                        == PaymentMethod.CARD
                                                ? "TXN-"
                                                        + UUID.randomUUID()
                                                                .toString()
                                                                .substring(0, 8)
                                                                .toUpperCase()
                                                : null)
                                .build();
                payment.setClinicId(clinicId);
                entityManager.persist(payment);
            }
        }

        log.info("Created invoices and payments");
    }

    private void initializeCertificates() {
        var certNumber = 1;
        String[] vaccineNames = {
            "Rabies Vaccine",
            "DHPP (Distemper, Hepatitis, Parainfluenza, Parvovirus)",
            "FVRCP (Feline Viral Rhinotracheitis, Calicivirus, Panleukopenia)",
            "Bordetella (Kennel Cough)",
            "Leptospirosis Vaccine",
            "Lyme Disease Vaccine"
        };
        String[] manufacturers = {
            "Zoetis", "Merck Animal Health", "Boehringer Ingelheim", "Elanco"
        };

        // Create vaccination certificates for some patients
        for (var patient : patients) {
            // 70% of patients have at least one certificate
            if (random.nextDouble() > 0.7) {
                continue;
            }

            // Find client for this patient
            var clientOpt =
                    clients.stream()
                            .filter(c -> c.getId().equals(patient.getOwnerId()))
                            .findFirst();
            if (clientOpt.isEmpty()) {
                continue;
            }
            var client = clientOpt.get();

            // Create 1-3 certificates per patient
            var numCertificates = 1 + random.nextInt(3);
            for (int i = 0; i < numCertificates; i++) {
                var certNum =
                        String.format("CERT-%d-%05d", LocalDate.now().getYear(), certNumber++);
                var adminDate = LocalDate.now().minusDays(random.nextInt(365));
                var expirationDate = adminDate.plusYears(1);
                var nextDueDate = expirationDate.minusDays(30);

                var vetIndex = random.nextInt(veterinarianIds.size());
                var vaccineName = vaccineNames[random.nextInt(vaccineNames.length)];

                // Determine certificate type based on vaccine
                CertificateType certType;
                if (vaccineName.contains("Rabies")) {
                    certType = CertificateType.RABIES;
                } else {
                    certType = CertificateType.VACCINATION;
                }

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
                                        manufacturers[random.nextInt(manufacturers.length)])
                                .batchNumber("BATCH-" + (1000 + random.nextInt(9000)))
                                .administrationDate(adminDate)
                                .expirationDate(expirationDate)
                                .nextDueDate(nextDueDate)
                                .veterinarianId(veterinarianIds.get(vetIndex))
                                .veterinarianName(veterinarianNames.get(vetIndex))
                                .veterinarianLicenseNumber("VET-" + (10000 + random.nextInt(90000)))
                                .isValid(!expirationDate.isBefore(LocalDate.now()))
                                .build();
                certificate.setClinicId(clinicId);
                entityManager.persist(certificate);
            }
        }

        log.info("Created vaccination certificates");
    }

    private void initializeGdprConsents() {
        var consentTypes = ConsentType.values();
        String[] consentTexts = {
            "I consent to the processing of my personal data for veterinary services.",
            "I consent to receive marketing communications about clinic services and promotions.",
            "I consent to the use of photos/videos of my pet for clinic promotional materials.",
            "I consent to medical procedures as recommended by the veterinarian.",
            "I consent to anesthesia administration if required for procedures.",
            "I consent to sharing medical data with specialist veterinarians if needed.",
            "I consent to the use of anonymized data for veterinary research."
        };

        // Create GDPR consents for all clients
        for (var client : clients) {
            // Each client gets 2-5 consent records
            var numConsents = 2 + random.nextInt(4);
            var usedTypes = new java.util.HashSet<ConsentType>();

            for (int i = 0; i < numConsents && usedTypes.size() < consentTypes.length; i++) {
                // Pick a random consent type not yet used
                ConsentType consentType;
                do {
                    consentType = consentTypes[random.nextInt(consentTypes.length)];
                } while (usedTypes.contains(consentType));
                usedTypes.add(consentType);

                var requestedAt = LocalDateTime.now().minusDays(random.nextInt(365));

                // Determine status
                ConsentStatus status;
                LocalDateTime grantedAt = null;
                LocalDateTime revokedAt = null;
                LocalDate expiresAt = null;

                var statusChance = random.nextDouble();
                if (statusChance < 0.75) {
                    // Granted
                    status = ConsentStatus.GRANTED;
                    grantedAt = requestedAt.plusMinutes(random.nextInt(60));
                    expiresAt = grantedAt.toLocalDate().plusYears(2);
                } else if (statusChance < 0.85) {
                    // Revoked
                    status = ConsentStatus.REVOKED;
                    grantedAt = requestedAt.plusMinutes(random.nextInt(60));
                    revokedAt = grantedAt.plusDays(30 + random.nextInt(180));
                } else if (statusChance < 0.95) {
                    // Pending
                    status = ConsentStatus.PENDING;
                } else {
                    // Expired
                    status = ConsentStatus.EXPIRED;
                    grantedAt = requestedAt.plusMinutes(random.nextInt(60));
                    expiresAt = LocalDate.now().minusDays(random.nextInt(30));
                }

                // Get appropriate consent text
                String consentText;
                switch (consentType) {
                    case DATA_PROCESSING -> consentText = consentTexts[0];
                    case MARKETING -> consentText = consentTexts[1];
                    case PHOTO_VIDEO -> consentText = consentTexts[2];
                    case MEDICAL_PROCEDURES -> consentText = consentTexts[3];
                    case ANESTHESIA -> consentText = consentTexts[4];
                    case DATA_SHARING -> consentText = consentTexts[5];
                    case RESEARCH -> consentText = consentTexts[6];
                    default -> consentText = "I consent to the terms and conditions.";
                }

                var consent =
                        GdprConsent.builder()
                                .clientId(client.getId())
                                .consentType(consentType)
                                .status(status)
                                .requestedAt(requestedAt)
                                .grantedAt(grantedAt)
                                .revokedAt(revokedAt)
                                .expiresAt(expiresAt)
                                .consentText(consentText)
                                .consentVersion("1.0")
                                .ipAddress("192.168.1." + (1 + random.nextInt(254)))
                                .build();
                consent.setClinicId(clinicId);
                entityManager.persist(consent);
            }
        }

        log.info("Created GDPR consents");
    }
}
