package com.vetclinic.config.seeder;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.veterinarian.domain.model.Veterinarian;

import lombok.extern.slf4j.Slf4j;

/** Seeds veterinarian entities. */
@Component
@Slf4j
public class VeterinarianSeeder implements DataSeeder {

    private static final String[][] VET_DATA = {
        {"Anna", "Kowalska", "anna.kowalska@happypaws.vet", "Surgery", "#4CAF50", "VET-001"},
        {"Piotr", "Nowak", "piotr.nowak@happypaws.vet", "Internal Medicine", "#2196F3", "VET-002"},
        {
            "Maria",
            "Wiśniewska",
            "maria.wisniewska@happypaws.vet",
            "Dermatology",
            "#9C27B0",
            "VET-003"
        },
        {"Jan", "Kowalczyk", "jan.kowalczyk@happypaws.vet", "Orthopedics", "#FF9800", "VET-004"},
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

    private static final int PHONE_BASE = 500_000_000;
    private static final int PHONE_RANGE = 99_999_999;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        for (var vet : VET_DATA) {
            var veterinarian = createVeterinarian(vet, context);
            entityManager.persist(veterinarian);
            context.addVeterinarian(veterinarian.getId(), veterinarian.getFullName());
        }
        log.info("Created {} veterinarians", context.getVeterinarianIds().size());
    }

    private Veterinarian createVeterinarian(String[] data, SeedContext context) {
        var phone = "+48 " + (PHONE_BASE + context.getRandom().nextInt(PHONE_RANGE));
        var veterinarian =
                Veterinarian.builder()
                        .firstName(data[0])
                        .lastName(data[1])
                        .email(data[2])
                        .phone(phone)
                        .specialization(data[3])
                        .colorCode(data[4])
                        .licenseNumber(data[5])
                        .active(true)
                        .build();
        veterinarian.setClinicId(context.getClinicId());
        return veterinarian;
    }
}
