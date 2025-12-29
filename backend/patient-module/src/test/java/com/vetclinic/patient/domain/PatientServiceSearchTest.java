package com.vetclinic.patient.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetclinic.common.event.DomainEventPublisher;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.Species;
import com.vetclinic.patient.domain.port.PatientRepository;

@ExtendWith(MockitoExtension.class)
class PatientServiceSearchTest {

    @Mock private PatientRepository patientRepository;
    @Mock private DomainEventPublisher eventPublisher;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository, eventPublisher);
    }

    @Nested
    class SearchByName {
        @Test
        void shouldSearchPatientsByName() {
            List<Patient> patients =
                    List.of(aPatient("Buddy", Species.DOG), aPatient("Buddy Jr", Species.DOG));
            given(patientRepository.findByNameContainingIgnoreCase("Buddy")).willReturn(patients);

            List<Patient> result = patientService.searchByName("Buddy");

            assertThat(result).hasSize(2);
            verify(patientRepository).findByNameContainingIgnoreCase("Buddy");
        }
    }

    @Nested
    class FindByMicrochip {
        @Test
        void shouldFindPatientByMicrochip() {
            String microchip = "123456789012345";
            Patient patient = aPatientWithMicrochip("Buddy", Species.DOG, microchip);
            given(patientRepository.findByMicrochipNumber(microchip))
                    .willReturn(Optional.of(patient));

            Optional<Patient> result = patientService.findByMicrochip(microchip);

            assertThat(result).isPresent();
            assertThat(result.get().getMicrochipNumber()).isEqualTo(microchip);
        }

        @Test
        void shouldReturnEmptyWhenMicrochipNotFound() {
            String microchip = "123456789012345";
            given(patientRepository.findByMicrochipNumber(microchip)).willReturn(Optional.empty());

            Optional<Patient> result = patientService.findByMicrochip(microchip);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetPatientsBySpecies {
        @Test
        void shouldGetPatientsBySpecies() {
            List<Patient> dogs =
                    List.of(aPatient("Buddy", Species.DOG), aPatient("Max", Species.DOG));
            given(patientRepository.findBySpecies(Species.DOG)).willReturn(dogs);

            List<Patient> result = patientService.getPatientsBySpecies(Species.DOG);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(p -> p.getSpecies() == Species.DOG);
        }
    }

    private static Patient aPatient(String name, Species species) {
        return Patient.builder()
                .name(name)
                .species(species)
                .breed("Unknown")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .labels(new HashSet<>())
                .build();
    }

    private static Patient aPatientWithMicrochip(String name, Species species, String microchip) {
        return Patient.builder()
                .name(name)
                .species(species)
                .breed("Unknown")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .microchipNumber(microchip)
                .labels(new HashSet<>())
                .build();
    }
}
