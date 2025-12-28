package com.vetclinic.veterinarian.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vetclinic.veterinarian.domain.model.VeterinarianDayOff;

@Repository
public interface JpaVeterinarianDayOffRepository extends JpaRepository<VeterinarianDayOff, UUID> {

    List<VeterinarianDayOff> findByVeterinarianId(UUID veterinarianId);

    @Query(
            "SELECT d FROM VeterinarianDayOff d WHERE d.veterinarianId = :veterinarianId "
                    + "AND ((d.startDate <= :endDate AND d.endDate >= :startDate))")
    List<VeterinarianDayOff> findByVeterinarianIdAndDateRange(
            @Param("veterinarianId") UUID veterinarianId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(
            "SELECT d FROM VeterinarianDayOff d WHERE d.veterinarianId = :veterinarianId "
                    + "AND d.startDate <= :date AND d.endDate >= :date")
    List<VeterinarianDayOff> findByVeterinarianIdAndDate(
            @Param("veterinarianId") UUID veterinarianId, @Param("date") LocalDate date);
}
