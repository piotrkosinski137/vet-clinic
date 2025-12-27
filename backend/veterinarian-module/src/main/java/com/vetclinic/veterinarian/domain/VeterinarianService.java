package com.vetclinic.veterinarian.domain;

import static com.vetclinic.common.validation.UniqueConstraintValidator.requireUnique;
import static com.vetclinic.common.validation.UniqueConstraintValidator.requireUniqueOnUpdate;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.common.event.DomainEventPublisher;
import com.vetclinic.common.util.ChangeDetector;
import com.vetclinic.veterinarian.domain.model.Veterinarian;
import com.vetclinic.veterinarian.domain.port.VeterinarianRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VeterinarianService {

    private static final String ENTITY_TYPE = "Veterinarian";

    private final VeterinarianRepository veterinarianRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public Veterinarian createVeterinarian(Veterinarian veterinarian) {
        requireUnique(
                veterinarianRepository::existsByEmail,
                veterinarian.getEmail(),
                () -> new VeterinarianEmailAlreadyExistsException(veterinarian.getEmail()));

        var saved = veterinarianRepository.save(veterinarian);
        eventPublisher.publishCreated(ENTITY_TYPE, saved.getId(), VeterinarianSnapshot.from(saved));
        return saved;
    }

    public Veterinarian getVeterinarian(UUID id) {
        return veterinarianRepository
                .findById(id)
                .orElseThrow(() -> new VeterinarianNotFoundException(id));
    }

    public Veterinarian getVeterinarianByEmail(String email) {
        return veterinarianRepository
                .findByEmail(email)
                .orElseThrow(() -> new VeterinarianNotFoundException(email));
    }

    public List<Veterinarian> getAllVeterinarians() {
        return veterinarianRepository.findAll();
    }

    public List<Veterinarian> getActiveVeterinarians() {
        return veterinarianRepository.findByActive(true);
    }

    public List<Veterinarian> getVeterinariansBySpecialization(String specialization) {
        return veterinarianRepository.findBySpecialization(specialization);
    }

    public List<Veterinarian> searchByName(String name) {
        return veterinarianRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Veterinarian updateVeterinarian(UUID id, Veterinarian updated) {
        var existing = getVeterinarian(id);
        var oldSnapshot = VeterinarianSnapshot.from(existing);

        var changedFields =
                ChangeDetector.comparing(existing, updated)
                        .check("firstName", Veterinarian::getFirstName)
                        .check("lastName", Veterinarian::getLastName)
                        .check("email", Veterinarian::getEmail)
                        .check("phone", Veterinarian::getPhone)
                        .check("specialization", Veterinarian::getSpecialization)
                        .check("licenseNumber", Veterinarian::getLicenseNumber)
                        .check("colorCode", Veterinarian::getColorCode)
                        .check("active", Veterinarian::getActive)
                        .check("notes", Veterinarian::getNotes)
                        .getChangedFields();

        // Validate email uniqueness only if changed
        if (changedFields.contains("email")) {
            requireUniqueOnUpdate(
                    existing.getEmail(),
                    updated.getEmail(),
                    veterinarianRepository::existsByEmail,
                    () -> new VeterinarianEmailAlreadyExistsException(updated.getEmail()));
        }

        applyVeterinarianUpdates(existing, updated);

        var saved = veterinarianRepository.save(existing);
        if (!changedFields.isEmpty()) {
            eventPublisher.publishUpdated(
                    ENTITY_TYPE, id, oldSnapshot, VeterinarianSnapshot.from(saved), changedFields);
        }
        return saved;
    }

    private void applyVeterinarianUpdates(Veterinarian existing, Veterinarian updated) {
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setSpecialization(updated.getSpecialization());
        existing.setLicenseNumber(updated.getLicenseNumber());
        existing.setColorCode(updated.getColorCode());
        existing.setActive(updated.getActive());
        existing.setNotes(updated.getNotes());
    }

    @Transactional
    public Veterinarian toggleActive(UUID id, Boolean active) {
        var veterinarian = getVeterinarian(id);
        if (Objects.equals(veterinarian.getActive(), active)) {
            return veterinarian;
        }

        var oldSnapshot = VeterinarianSnapshot.from(veterinarian);
        veterinarian.setActive(active);
        var saved = veterinarianRepository.save(veterinarian);

        eventPublisher.publishUpdated(
                ENTITY_TYPE, id, oldSnapshot, VeterinarianSnapshot.from(saved), Set.of("active"));
        return saved;
    }

    @Transactional
    public void deleteVeterinarian(UUID id) {
        var veterinarian =
                veterinarianRepository
                        .findById(id)
                        .orElseThrow(() -> new VeterinarianNotFoundException(id));

        var snapshot = VeterinarianSnapshot.from(veterinarian);
        veterinarianRepository.deleteById(id);
        eventPublisher.publishDeleted(ENTITY_TYPE, id, snapshot);
    }
}
