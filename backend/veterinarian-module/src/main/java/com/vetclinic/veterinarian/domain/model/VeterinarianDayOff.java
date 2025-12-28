package com.vetclinic.veterinarian.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A period when a veterinarian is not available (vacation, sick leave, etc.). */
@Entity
@Table(name = "veterinarian_days_off")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarianDayOff extends TenantAwareEntity {

    @Column(name = "veterinarian_id", nullable = false)
    private UUID veterinarianId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DayOffType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Whether this day off has been approved */
    @Builder.Default
    @Column(nullable = false)
    private boolean approved = false;

    /** Check if a given date falls within this day off period */
    public boolean containsDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /** Get the total number of days in this period */
    public long getDaysCount() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
