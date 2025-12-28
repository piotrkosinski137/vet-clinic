package com.vetclinic.veterinarian.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vetclinic.veterinarian.domain.model.VeterinarianDayOff;
import com.vetclinic.veterinarian.domain.port.VeterinarianDayOffRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VeterinarianDayOffRepositoryAdapter implements VeterinarianDayOffRepository {

    private final JpaVeterinarianDayOffRepository jpaRepository;

    @Override
    public VeterinarianDayOff save(VeterinarianDayOff dayOff) {
        return jpaRepository.save(dayOff);
    }

    @Override
    public Optional<VeterinarianDayOff> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<VeterinarianDayOff> findByVeterinarianId(UUID veterinarianId) {
        return jpaRepository.findByVeterinarianId(veterinarianId);
    }

    @Override
    public List<VeterinarianDayOff> findByVeterinarianIdAndDateRange(
            UUID veterinarianId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByVeterinarianIdAndDateRange(veterinarianId, startDate, endDate);
    }

    @Override
    public List<VeterinarianDayOff> findByVeterinarianIdAndDate(
            UUID veterinarianId, LocalDate date) {
        return jpaRepository.findByVeterinarianIdAndDate(veterinarianId, date);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
