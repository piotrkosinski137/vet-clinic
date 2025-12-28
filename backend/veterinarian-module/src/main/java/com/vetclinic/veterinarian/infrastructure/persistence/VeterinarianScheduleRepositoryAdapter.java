package com.vetclinic.veterinarian.infrastructure.persistence;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vetclinic.veterinarian.domain.model.VeterinarianSchedule;
import com.vetclinic.veterinarian.domain.port.VeterinarianScheduleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VeterinarianScheduleRepositoryAdapter implements VeterinarianScheduleRepository {

    private final JpaVeterinarianScheduleRepository jpaRepository;

    @Override
    public VeterinarianSchedule save(VeterinarianSchedule schedule) {
        return jpaRepository.save(schedule);
    }

    @Override
    public List<VeterinarianSchedule> saveAll(List<VeterinarianSchedule> schedules) {
        return jpaRepository.saveAll(schedules);
    }

    @Override
    public Optional<VeterinarianSchedule> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<VeterinarianSchedule> findByVeterinarianId(UUID veterinarianId) {
        return jpaRepository.findByVeterinarianId(veterinarianId);
    }

    @Override
    public Optional<VeterinarianSchedule> findByVeterinarianIdAndDayOfWeek(
            UUID veterinarianId, DayOfWeek dayOfWeek) {
        return jpaRepository.findByVeterinarianIdAndDayOfWeek(veterinarianId, dayOfWeek);
    }

    @Override
    public void deleteByVeterinarianId(UUID veterinarianId) {
        jpaRepository.deleteByVeterinarianId(veterinarianId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByVeterinarianId(UUID veterinarianId) {
        return jpaRepository.existsByVeterinarianId(veterinarianId);
    }
}
