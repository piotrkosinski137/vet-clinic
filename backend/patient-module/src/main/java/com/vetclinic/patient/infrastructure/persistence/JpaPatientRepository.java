package com.vetclinic.patient.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetclinic.patient.domain.model.Patient;

interface JpaPatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findByOwnerId(UUID ownerId);
}
