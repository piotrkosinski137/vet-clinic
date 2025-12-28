package com.vetclinic.config.seeder;

import java.util.Comparator;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates data initialization by running all DataSeeder components in order. This class
 * follows the Single Responsibility Principle by delegating actual seeding to specialized seeders.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializerOrchestrator implements ApplicationRunner {

    private final EntityManager entityManager;
    private final List<DataSeeder> seeders;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting data initialization...");

        if (dataAlreadyExists()) {
            log.info("Data already exists, clearing and reinitializing...");
            clearAllData();
        }

        var context = new SeedContext();
        runSeeders(context);

        logSummary();
        log.info("Data initialization completed successfully!");
    }

    private boolean dataAlreadyExists() {
        var clinicCount =
                entityManager
                        .createQuery("SELECT COUNT(c) FROM VeterinaryClinic c", Long.class)
                        .getSingleResult();
        return clinicCount > 0;
    }

    private void clearAllData() {
        // Order matters due to foreign key constraints
        entityManager.createQuery("DELETE FROM Payment").executeUpdate();
        entityManager.createQuery("DELETE FROM Invoice").executeUpdate();
        entityManager.createQuery("DELETE FROM VaccinationCertificate").executeUpdate();
        entityManager.createQuery("DELETE FROM GdprConsent").executeUpdate();
        entityManager.createQuery("DELETE FROM Visit").executeUpdate();
        entityManager.createQuery("DELETE FROM Patient").executeUpdate();
        entityManager.createQuery("DELETE FROM Client").executeUpdate();
        entityManager.createQuery("DELETE FROM VeterinarianSchedule").executeUpdate();
        entityManager.createQuery("DELETE FROM VeterinarianDayOff").executeUpdate();
        entityManager.createQuery("DELETE FROM Veterinarian").executeUpdate();
        entityManager.createQuery("DELETE FROM PriceListItem").executeUpdate();
        entityManager.createQuery("DELETE FROM AuditLog").executeUpdate();
        entityManager.createQuery("DELETE FROM VeterinaryClinic").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    private void runSeeders(SeedContext context) {
        seeders.stream()
                .sorted(Comparator.comparingInt(DataSeeder::getOrder))
                .forEach(
                        seeder -> {
                            log.debug("Running seeder: {}", seeder.getClass().getSimpleName());
                            seeder.seed(entityManager, context);
                        });
    }

    private void logSummary() {
        log.info(
                "Created: {} veterinarians, {} clients, {} patients, {} visits",
                countEntity("Veterinarian"),
                countEntity("Client"),
                countEntity("Patient"),
                countEntity("Visit"));
        log.info(
                "Created: {} invoices, {} payments, {} certificates, {} GDPR consents",
                countEntity("Invoice"),
                countEntity("Payment"),
                countEntity("VaccinationCertificate"),
                countEntity("GdprConsent"));
    }

    private Long countEntity(String entityName) {
        return entityManager
                .createQuery("SELECT COUNT(e) FROM " + entityName + " e", Long.class)
                .getSingleResult();
    }
}
