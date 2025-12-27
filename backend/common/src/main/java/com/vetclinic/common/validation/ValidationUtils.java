package com.vetclinic.common.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Utility class for common validation operations. */
public final class ValidationUtils {

    private ValidationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates that an object is not null.
     *
     * @param obj the object to validate
     * @param fieldName the field name for error message
     * @throws IllegalArgumentException if obj is null
     */
    public static void requireNonNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    /**
     * Validates that a UUID is not null.
     *
     * @param id the UUID to validate
     * @param fieldName the field name for error message
     * @throws IllegalArgumentException if id is null
     */
    public static void requireValidId(UUID id, String fieldName) {
        if (id == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    /**
     * Validates that a string is not null or blank.
     *
     * @param value the string to validate
     * @param fieldName the field name for error message
     * @throws IllegalArgumentException if value is null or blank
     */
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    /**
     * Validates that a BigDecimal is positive (greater than zero).
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @throws IllegalArgumentException if value is null or not positive
     */
    public static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    /**
     * Validates that a BigDecimal is not negative (zero or positive).
     *
     * @param value the value to validate
     * @param fieldName the field name for error message
     * @throws IllegalArgumentException if value is null or negative
     */
    public static void requireNotNegative(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    /**
     * Validates that a date range is valid (from is not after to).
     *
     * @param from the start date (can be null)
     * @param to the end date (can be null)
     * @throws IllegalArgumentException if from is after to
     */
    public static void requireValidDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    /**
     * Validates that a value does not exceed a maximum.
     *
     * @param value the value to validate
     * @param max the maximum allowed value
     * @param fieldName the field name for error message
     * @throws IllegalArgumentException if value exceeds max
     */
    public static void requireNotExceed(BigDecimal value, BigDecimal max, String fieldName) {
        if (value != null && max != null && value.compareTo(max) > 0) {
            throw new IllegalArgumentException(fieldName + " exceeds maximum allowed: " + max);
        }
    }

    /**
     * Validates that a number is within a range.
     *
     * @param value the value to validate
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @param fieldName the field name for error message
     * @throws IllegalArgumentException if value is outside the range
     */
    public static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max);
        }
    }
}
