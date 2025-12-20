package com.vetclinic.patient.infrastructure.persistence;

import com.vetclinic.patient.domain.model.Patient;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaPatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findByOwnerId(UUID ownerId);
}
