package com.vetclinic.veterinarian.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vetclinic.veterinarian.domain.model.Veterinarian;
import com.vetclinic.veterinarian.domain.port.VeterinarianRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VeterinarianRepositoryAdapter implements VeterinarianRepository {

    private final JpaVeterinarianRepository jpaRepository;

    @Override
    public Veterinarian save(Veterinarian veterinarian) {
        return jpaRepository.save(veterinarian);
    }

    @Override
    public Optional<Veterinarian> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Veterinarian> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public List<Veterinarian> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Veterinarian> findByActive(Boolean active) {
        return jpaRepository.findByActive(active);
    }

    @Override
    public List<Veterinarian> findBySpecialization(String specialization) {
        return jpaRepository.findBySpecialization(specialization);
    }

    @Override
    public List<Veterinarian> findByNameContainingIgnoreCase(String name) {
        return jpaRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
