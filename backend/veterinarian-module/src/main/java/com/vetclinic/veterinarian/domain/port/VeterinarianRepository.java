package com.vetclinic.veterinarian.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.veterinarian.domain.model.Veterinarian;

/** Port for veterinarian persistence operations. */
public interface VeterinarianRepository {

    Veterinarian save(Veterinarian veterinarian);

    Optional<Veterinarian> findById(UUID id);

    Optional<Veterinarian> findByEmail(String email);

    List<Veterinarian> findAll();

    List<Veterinarian> findByActive(Boolean active);

    List<Veterinarian> findBySpecialization(String specialization);

    List<Veterinarian> findByNameContainingIgnoreCase(String name);

    boolean existsById(UUID id);

    boolean existsByEmail(String email);

    void deleteById(UUID id);
}
