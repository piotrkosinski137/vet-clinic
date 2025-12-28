package com.vetclinic.veterinarian.domain.port;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.veterinarian.domain.model.VeterinarianSchedule;

/** Port for veterinarian schedule persistence operations. */
public interface VeterinarianScheduleRepository {

    VeterinarianSchedule save(VeterinarianSchedule schedule);

    List<VeterinarianSchedule> saveAll(List<VeterinarianSchedule> schedules);

    Optional<VeterinarianSchedule> findById(UUID id);

    List<VeterinarianSchedule> findByVeterinarianId(UUID veterinarianId);

    Optional<VeterinarianSchedule> findByVeterinarianIdAndDayOfWeek(
            UUID veterinarianId, DayOfWeek dayOfWeek);

    void deleteByVeterinarianId(UUID veterinarianId);

    void deleteById(UUID id);

    boolean existsByVeterinarianId(UUID veterinarianId);
}
