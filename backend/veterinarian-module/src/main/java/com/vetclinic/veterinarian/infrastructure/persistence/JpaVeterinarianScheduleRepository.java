package com.vetclinic.veterinarian.infrastructure.persistence;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vetclinic.veterinarian.domain.model.VeterinarianSchedule;

@Repository
public interface JpaVeterinarianScheduleRepository
        extends JpaRepository<VeterinarianSchedule, UUID> {

    List<VeterinarianSchedule> findByVeterinarianId(UUID veterinarianId);

    Optional<VeterinarianSchedule> findByVeterinarianIdAndDayOfWeek(
            UUID veterinarianId, DayOfWeek dayOfWeek);

    @Modifying
    @Query("DELETE FROM VeterinarianSchedule s WHERE s.veterinarianId = :veterinarianId")
    void deleteByVeterinarianId(@Param("veterinarianId") UUID veterinarianId);

    boolean existsByVeterinarianId(UUID veterinarianId);
}
