package com.vetclinic.patient.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetclinic.common.event.DomainEventPublisher;
import com.vetclinic.common.exception.ResourceNotFoundException;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.Species;
import com.vetclinic.patient.domain.port.PatientRepository;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private DomainEventPublisher eventPublisher;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository, eventPublisher);
    }

    @Test
    void shouldCreatePatient() {
        // given
        Patient patient = aPatient("Buddy", Species.DOG);
        given(patientRepository.save(any(Patient.class))).willReturn(patient);

        // when
        Patient result = patientService.createPatient(patient);

        // then
        assertThat(result.getName()).isEqualTo("Buddy");
        verify(patientRepository).save(patient);
    }

    @Test
    void shouldGetPatientById() {
        // given
        UUID id = UUID.randomUUID();
        Patient patient = aPatient("Whiskers", Species.CAT);
        given(patientRepository.findById(id)).willReturn(Optional.of(patient));

        // when
        Patient result = patientService.getPatient(id);

        // then
        assertThat(result.getName()).isEqualTo("Whiskers");
    }

    @Test
    void shouldThrowWhenPatientNotFound() {
        // given
        UUID id = UUID.randomUUID();
        given(patientRepository.findById(id)).willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> patientService.getPatient(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldGetAllPatients() {
        // given
        List<Patient> patients =
                List.of(aPatient("Buddy", Species.DOG), aPatient("Whiskers", Species.CAT));
        given(patientRepository.findAll()).willReturn(patients);

        // when
        List<Patient> result = patientService.getAllPatients();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldGetPatientsByOwner() {
        // given
        UUID ownerId = UUID.randomUUID();
        List<Patient> patients = List.of(aPatient("Buddy", Species.DOG));
        given(patientRepository.findByOwnerId(ownerId)).willReturn(patients);

        // when
        List<Patient> result = patientService.getPatientsByOwner(ownerId);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldDeletePatient() {
        // given
        UUID id = UUID.randomUUID();
        Patient patient = aPatient("Buddy", Species.DOG);
        given(patientRepository.findById(id)).willReturn(Optional.of(patient));

        // when
        patientService.deletePatient(id);

        // then
        verify(patientRepository).deleteById(id);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentPatient() {
        // given
        UUID id = UUID.randomUUID();
        given(patientRepository.findById(id)).willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> patientService.deletePatient(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Patient aPatient(String name, Species species) {
        return Patient.builder()
                .name(name)
                .species(species)
                .breed("Unknown")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .build();
    }
}
