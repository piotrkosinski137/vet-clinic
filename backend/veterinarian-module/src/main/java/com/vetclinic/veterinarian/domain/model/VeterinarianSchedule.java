package com.vetclinic.veterinarian.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Weekly schedule entry for a veterinarian. One record per day of week defines working hours or
 * indicates a non-working day.
 */
@Entity
@Table(
        name = "veterinarian_schedules",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_vet_schedule_day",
                        columnNames = {"clinic_id", "veterinarian_id", "day_of_week"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarianSchedule extends TenantAwareEntity {

    @Column(name = "veterinarian_id", nullable = false)
    private UUID veterinarianId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    /** Start time of work (null if not a working day) */
    @Column(name = "start_time")
    private LocalTime startTime;

    /** End time of work (null if not a working day) */
    @Column(name = "end_time")
    private LocalTime endTime;

    /** Whether this is a working day */
    @Builder.Default
    @Column(name = "is_working_day", nullable = false)
    private boolean workingDay = true;

    /** Check if a given time falls within working hours */
    public boolean isWithinWorkingHours(LocalTime time) {
        if (!workingDay || startTime == null || endTime == null) {
            return false;
        }
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }
}
