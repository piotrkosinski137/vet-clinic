package com.vetclinic.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Utility class for common date and time operations. */
public final class DateTimeUtils {

    private DateTimeUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the start of day (midnight) for a given date.
     *
     * @param date the date
     * @return LocalDateTime at start of day
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * Gets the end of day (start of next day) for a given date. This is useful for exclusive end
     * ranges in queries.
     *
     * @param date the date
     * @return LocalDateTime at start of next day
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.plusDays(1).atStartOfDay();
    }

    /**
     * Checks if two time ranges overlap.
     *
     * @param start1 start of first range
     * @param end1 end of first range
     * @param start2 start of second range
     * @param end2 end of second range
     * @return true if ranges overlap
     */
    public static boolean overlaps(
            LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Calculates the end time based on start time and duration in minutes.
     *
     * @param startTime the start time
     * @param durationMinutes the duration in minutes
     * @return the end time
     */
    public static LocalDateTime calculateEndTime(LocalDateTime startTime, int durationMinutes) {
        if (startTime == null) {
            return null;
        }
        return startTime.plusMinutes(durationMinutes);
    }

    /**
     * Gets the date key string (YYYY-MM-DD) for grouping/lookup.
     *
     * @param date the date
     * @return date string in ISO format
     */
    public static String getDateKey(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.toString();
    }

    /**
     * Gets the date key from a LocalDateTime.
     *
     * @param dateTime the date time
     * @return date string in ISO format
     */
    public static String getDateKey(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().toString();
    }
}
