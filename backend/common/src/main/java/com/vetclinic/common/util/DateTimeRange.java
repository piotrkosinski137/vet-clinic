package com.vetclinic.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable value object representing a date-time range with start (inclusive) and end (exclusive).
 * Useful for date range queries, scheduling, and time-based filtering.
 */
public record DateTimeRange(LocalDateTime start, LocalDateTime end) {

    public DateTimeRange {
        Objects.requireNonNull(start, "Start cannot be null");
        Objects.requireNonNull(end, "End cannot be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End must be after or equal to start");
        }
    }

    /**
     * Creates a range covering an entire day (00:00:00 to next day 00:00:00).
     *
     * @param date the date for the range
     * @return DateTimeRange for the entire day
     */
    public static DateTimeRange forDay(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");
        return new DateTimeRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    /**
     * Creates a range covering multiple days (inclusive of startDate, exclusive of day after
     * endDate).
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return DateTimeRange covering the date range
     */
    public static DateTimeRange forDateRange(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        return new DateTimeRange(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    /**
     * Checks if a given date-time falls within this range (start inclusive, end exclusive).
     *
     * @param dateTime the date-time to check
     * @return true if dateTime is within the range
     */
    public boolean contains(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return !dateTime.isBefore(start) && dateTime.isBefore(end);
    }

    /**
     * Checks if a given date falls within this range.
     *
     * @param date the date to check
     * @return true if any part of the date falls within the range
     */
    public boolean contains(LocalDate date) {
        if (date == null) {
            return false;
        }
        return contains(date.atStartOfDay());
    }
}
