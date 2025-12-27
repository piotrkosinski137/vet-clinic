package com.vetclinic.patient.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;

public interface JpaPatientRepository
        extends JpaRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {

    List<Patient> findByOwnerId(UUID ownerId);

    List<Patient> findByNameContainingIgnoreCase(String name);

    Optional<Patient> findByMicrochipNumber(String microchipNumber);

    List<Patient> findBySpecies(Species species);

    @Query("SELECT DISTINCT p FROM Patient p JOIN p.labels l WHERE l IN :labels")
    List<Patient> findByLabelsIn(@Param("labels") Iterable<PatientLabel> labels);

    @Query(
            """
            SELECT p FROM Patient p
            WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
            AND (:species IS NULL OR p.species = :species)
            AND (:breed IS NULL OR LOWER(p.breed) LIKE LOWER(CONCAT('%', CAST(:breed AS string), '%')))
            AND (:ownerId IS NULL OR p.ownerId = :ownerId)
            AND (:microchipNumber IS NULL OR p.microchipNumber = CAST(:microchipNumber AS string))
            """)
    List<Patient> search(
            @Param("name") String name,
            @Param("species") Species species,
            @Param("breed") String breed,
            @Param("ownerId") UUID ownerId,
            @Param("microchipNumber") String microchipNumber);
}
