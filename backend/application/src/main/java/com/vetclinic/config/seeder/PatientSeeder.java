package com.vetclinic.config.seeder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.patient.domain.model.Gender;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;

import lombok.extern.slf4j.Slf4j;

/** Seeds patient entities. */
@Component
@Slf4j
public class PatientSeeder implements DataSeeder {

    private static final int SENIOR_AGE_THRESHOLD = 7;
    private static final int DAYS_IN_YEAR = 365;
    private static final long MICROCHIP_MAX = 9_999_999_999_999L;

    private static final double VIP_PROBABILITY = 0.1;
    private static final double AGGRESSIVE_PROBABILITY = 0.15;
    private static final double ALLERGIC_PROBABILITY = 0.1;
    private static final double CHRONIC_PROBABILITY = 0.15;

    @Override
    public int getOrder() {
        return 4;
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        seedDogs(entityManager, context);
        seedCats(entityManager, context);
        seedOtherAnimals(entityManager, context);
        log.info("Created {} patients", context.getPatientIds().size());
    }

    private void seedDogs(EntityManager entityManager, SeedContext context) {
        createPatient(
                entityManager,
                context,
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
                entityManager,
                context,
                "Luna",
                Species.DOG,
                "Golden Retriever",
                Gender.FEMALE,
                28.0,
                "Golden",
                true,
                3,
                1);
        createPatient(
                entityManager,
                context,
                "Max",
                Species.DOG,
                "Labrador",
                Gender.MALE,
                32.0,
                "Black",
                true,
                7,
                2);
        createPatient(
                entityManager,
                context,
                "Bella",
                Species.DOG,
                "Beagle",
                Gender.FEMALE,
                12.5,
                "Tricolor",
                false,
                2,
                3);
        createPatient(
                entityManager,
                context,
                "Rocky",
                Species.DOG,
                "Boxer",
                Gender.MALE,
                30.0,
                "Fawn",
                true,
                4,
                4);
        createPatient(
                entityManager,
                context,
                "Mila",
                Species.DOG,
                "French Bulldog",
                Gender.FEMALE,
                11.0,
                "Brindle",
                true,
                1,
                5);
        createPatient(
                entityManager,
                context,
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
                entityManager,
                context,
                "Kira",
                Species.DOG,
                "Husky",
                Gender.FEMALE,
                22.0,
                "Gray and white",
                false,
                3,
                7);
        createPatient(
                entityManager,
                context,
                "Rex",
                Species.DOG,
                "German Shepherd",
                Gender.MALE,
                38.0,
                "Sable",
                true,
                8,
                8);
        createPatient(
                entityManager,
                context,
                "Nala",
                Species.DOG,
                "Poodle",
                Gender.FEMALE,
                8.0,
                "White",
                true,
                4,
                9);
    }

    private void seedCats(EntityManager entityManager, SeedContext context) {
        createPatient(
                entityManager,
                context,
                "Mruczek",
                Species.CAT,
                "Persian",
                Gender.MALE,
                5.5,
                "White",
                true,
                6,
                10);
        createPatient(
                entityManager,
                context,
                "Kicia",
                Species.CAT,
                "Siamese",
                Gender.FEMALE,
                4.0,
                "Seal point",
                true,
                3,
                11);
        createPatient(
                entityManager,
                context,
                "Filemon",
                Species.CAT,
                "Maine Coon",
                Gender.MALE,
                8.5,
                "Brown tabby",
                true,
                5,
                12);
        createPatient(
                entityManager,
                context,
                "Puszek",
                Species.CAT,
                "British Shorthair",
                Gender.MALE,
                6.0,
                "Blue",
                true,
                2,
                13);
        createPatient(
                entityManager,
                context,
                "Misia",
                Species.CAT,
                "Ragdoll",
                Gender.FEMALE,
                5.0,
                "Blue bicolor",
                false,
                1,
                14);
        createPatient(
                entityManager,
                context,
                "Tygrys",
                Species.CAT,
                "Bengal",
                Gender.MALE,
                5.5,
                "Spotted",
                true,
                4,
                15);
        createPatient(
                entityManager,
                context,
                "Cleo",
                Species.CAT,
                "Abyssinian",
                Gender.FEMALE,
                3.5,
                "Ruddy",
                true,
                2,
                16);
    }

    private void seedOtherAnimals(EntityManager entityManager, SeedContext context) {
        createPatient(
                entityManager,
                context,
                "Tweetie",
                Species.BIRD,
                "Canary",
                Gender.FEMALE,
                0.025,
                "Yellow",
                false,
                2,
                0);
        createPatient(
                entityManager,
                context,
                "Coco",
                Species.BIRD,
                "Cockatiel",
                Gender.MALE,
                0.09,
                "Gray",
                false,
                5,
                1);
        createPatient(
                entityManager,
                context,
                "Bunny",
                Species.RABBIT,
                "Holland Lop",
                Gender.FEMALE,
                2.0,
                "White and brown",
                false,
                1,
                2);
        createPatient(
                entityManager,
                context,
                "Flopsy",
                Species.RABBIT,
                "Mini Rex",
                Gender.MALE,
                1.8,
                "Black",
                true,
                3,
                3);
        createPatient(
                entityManager,
                context,
                "Chomik",
                Species.HAMSTER,
                "Syrian",
                Gender.MALE,
                0.15,
                "Golden",
                false,
                1,
                4);
        createPatient(
                entityManager,
                context,
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
                entityManager,
                context,
                "Nemo",
                Species.FISH,
                "Clownfish",
                Gender.MALE,
                0.02,
                "Orange and white",
                false,
                2,
                6);
    }

    private void createPatient(
            EntityManager entityManager,
            SeedContext context,
            String name,
            Species species,
            String breed,
            Gender gender,
            double weight,
            String color,
            boolean neutered,
            int ageYears,
            int ownerIndex) {

        var random = context.getRandom();
        var dateOfBirth =
                LocalDate.now().minusYears(ageYears).minusDays(random.nextInt(DAYS_IN_YEAR));
        var ownerId = context.getClientIds().get(ownerIndex % context.getClientIds().size());
        var microchip = generateMicrochip(species, random);
        var labels = generateLabels(species, ageYears, random);

        var patient =
                Patient.builder()
                        .name(name)
                        .species(species)
                        .breed(breed)
                        .gender(gender)
                        .weight(weight)
                        .color(color)
                        .neutered(neutered)
                        .dateOfBirth(dateOfBirth)
                        .ownerId(ownerId)
                        .microchipNumber(microchip)
                        .labels(labels)
                        .build();
        patient.setClinicId(context.getClinicId());
        entityManager.persist(patient);
        context.addPatient(patient);
    }

    private String generateMicrochip(Species species, java.util.Random random) {
        if (species == Species.DOG || species == Species.CAT) {
            return "PL" + String.format("%013d", random.nextLong(MICROCHIP_MAX));
        }
        return null;
    }

    private Set<PatientLabel> generateLabels(
            Species species, int ageYears, java.util.Random random) {
        Set<PatientLabel> labels = new HashSet<>();
        if (ageYears >= SENIOR_AGE_THRESHOLD
                && (species == Species.DOG || species == Species.CAT)) {
            labels.add(PatientLabel.SENIOR);
        }
        if (random.nextDouble() < VIP_PROBABILITY) {
            labels.add(PatientLabel.VIP);
        }
        if (random.nextDouble() < AGGRESSIVE_PROBABILITY) {
            labels.add(PatientLabel.AGGRESSIVE);
        }
        if (random.nextDouble() < ALLERGIC_PROBABILITY) {
            labels.add(PatientLabel.ALLERGIC);
        }
        if (random.nextDouble() < CHRONIC_PROBABILITY) {
            labels.add(PatientLabel.CHRONIC);
        }
        return labels;
    }
}
