package com.vetclinic.common.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Fluent utility for detecting field changes between two entity states. Used for audit logging to
 * track which fields were modified.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * var changedFields = ChangeDetector.comparing(existing, updated)
 *     .check("name", Patient::getName)
 *     .check("species", Patient::getSpecies)
 *     .check("breed", Patient::getBreed)
 *     .getChangedFields();
 * }</pre>
 *
 * @param <T> the entity type being compared
 */
public class ChangeDetector<T> {

    private final T oldEntity;
    private final T newEntity;
    private final Set<String> changedFields = new HashSet<>();

    private ChangeDetector(T oldEntity, T newEntity) {
        this.oldEntity = oldEntity;
        this.newEntity = newEntity;
    }

    /**
     * Creates a new ChangeDetector comparing two entity states.
     *
     * @param oldEntity the original entity state
     * @param newEntity the updated entity state
     * @param <T> the entity type
     * @return a new ChangeDetector instance
     */
    public static <T> ChangeDetector<T> comparing(T oldEntity, T newEntity) {
        return new ChangeDetector<>(oldEntity, newEntity);
    }

    /**
     * Checks if a field has changed and records it if so.
     *
     * @param fieldName the name of the field for audit logging
     * @param getter the getter function to extract the field value
     * @param <V> the field value type
     * @return this detector for method chaining
     */
    public <V> ChangeDetector<T> check(String fieldName, Function<T, V> getter) {
        var oldValue = getter.apply(oldEntity);
        var newValue = getter.apply(newEntity);
        if (!Objects.equals(oldValue, newValue)) {
            changedFields.add(fieldName);
        }
        return this;
    }

    /**
     * Returns an immutable copy of the detected changed fields.
     *
     * @return set of field names that have changed
     */
    public Set<String> getChangedFields() {
        return Set.copyOf(changedFields);
    }

    /**
     * Returns true if any fields have changed.
     *
     * @return true if at least one field has changed
     */
    public boolean hasChanges() {
        return !changedFields.isEmpty();
    }

    /**
     * Returns the number of changed fields.
     *
     * @return count of changed fields
     */
    public int changeCount() {
        return changedFields.size();
    }
}
