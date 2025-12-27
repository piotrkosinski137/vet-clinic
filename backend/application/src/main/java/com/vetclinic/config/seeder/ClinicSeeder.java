package com.vetclinic.config.seeder;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** Seeds the veterinary clinic entity. */
@Component
@Slf4j
public class ClinicSeeder implements DataSeeder {

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        entityManager
                .createNativeQuery(
                        """
                        INSERT INTO veterinary_clinics (id, name, slug, email, phone, address, city, postal_code, active, created_at)
                        VALUES (:id, :name, :slug, :email, :phone, :address, :city, :postalCode, :active, CURRENT_TIMESTAMP)
                        """)
                .setParameter("id", context.getDefaultClinicId())
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
        context.setClinicId(context.getDefaultClinicId());
        log.info("Created clinic: Happy Paws Veterinary Clinic (ID: {})", context.getClinicId());
    }
}
