package com.vetclinic.veterinarian.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vetclinic.veterinarian.domain.model.Veterinarian;

@Repository
public interface JpaVeterinarianRepository extends JpaRepository<Veterinarian, UUID> {

    Optional<Veterinarian> findByEmail(String email);

    List<Veterinarian> findByActive(Boolean active);

    List<Veterinarian> findBySpecialization(String specialization);

    @Query(
            "SELECT v FROM Veterinarian v WHERE LOWER(v.firstName) LIKE LOWER(CONCAT('%', :name, '%')) "
                    + "OR LOWER(v.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Veterinarian> findByNameContainingIgnoreCase(@Param("name") String name);

    boolean existsByEmail(String email);
}
