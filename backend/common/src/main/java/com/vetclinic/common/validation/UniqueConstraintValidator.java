package com.vetclinic.common.validation;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utility class for validating unique constraints. Helps enforce DRY principle when checking
 * uniqueness of fields like email addresses across different entities.
 */
public final class UniqueConstraintValidator {

    private UniqueConstraintValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates that a value is unique on creation. Throws the provided exception if the value
     * already exists.
     *
     * @param existsCheck predicate to check if the value exists
     * @param value the value to check
     * @param exceptionSupplier supplier for the exception to throw if value exists
     * @param <E> the type of exception to throw
     */
    public static <E extends RuntimeException> void requireUnique(
            Predicate<String> existsCheck, String value, Supplier<E> exceptionSupplier) {
        if (existsCheck.test(value)) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Validates that a value is unique on update (only if changed). Skips the check if the value
     * hasn't changed, otherwise throws the provided exception if the new value already exists.
     *
     * @param existingValue the current value (before update)
     * @param newValue the new value (after update)
     * @param existsCheck predicate to check if the value exists
     * @param exceptionSupplier supplier for the exception to throw if value exists
     * @param <E> the type of exception to throw
     */
    public static <E extends RuntimeException> void requireUniqueOnUpdate(
            String existingValue,
            String newValue,
            Predicate<String> existsCheck,
            Supplier<E> exceptionSupplier) {
        if (!existingValue.equals(newValue) && existsCheck.test(newValue)) {
            throw exceptionSupplier.get();
        }
    }
}
