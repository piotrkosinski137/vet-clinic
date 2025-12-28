package com.vetclinic.veterinarian.domain.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.veterinarian.domain.model.VeterinarianDayOff;

/** Port for veterinarian day off persistence operations. */
public interface VeterinarianDayOffRepository {

    VeterinarianDayOff save(VeterinarianDayOff dayOff);

    Optional<VeterinarianDayOff> findById(UUID id);

    List<VeterinarianDayOff> findByVeterinarianId(UUID veterinarianId);

    List<VeterinarianDayOff> findByVeterinarianIdAndDateRange(
            UUID veterinarianId, LocalDate startDate, LocalDate endDate);

    /** Find all days off that overlap with a specific date */
    List<VeterinarianDayOff> findByVeterinarianIdAndDate(UUID veterinarianId, LocalDate date);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
