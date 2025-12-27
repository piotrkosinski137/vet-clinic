package com.vetclinic.common.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for updating entity fields with consistent null handling and default values.
 *
 * <p>Provides a fluent API for entity updates, reducing boilerplate while maintaining clarity.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * EntityUpdater.from(updated)
 *     .update(existing::setName, Visit::getName)
 *     .updateWithDefault(existing::setDuration, Visit::getDuration, 30)
 *     .updateIfNotNull(existing::setNotes, Visit::getNotes);
 * }</pre>
 *
 * @param <S> the source entity type
 */
public class EntityUpdater<S> {

    private final S source;

    private EntityUpdater(S source) {
        this.source = source;
    }

    /**
     * Creates a new EntityUpdater for the given source entity.
     *
     * @param source the source entity to get values from
     * @param <S> the source entity type
     * @return a new EntityUpdater instance
     */
    public static <S> EntityUpdater<S> from(S source) {
        return new EntityUpdater<>(source);
    }

    /**
     * Updates the target field with the value from source. Always copies the value.
     *
     * @param setter the setter on the target entity
     * @param getter the getter from the source entity
     * @param <V> the value type
     * @return this updater for chaining
     */
    public <V> EntityUpdater<S> update(Consumer<V> setter, Function<S, V> getter) {
        setter.accept(getter.apply(source));
        return this;
    }

    /**
     * Updates the target field with the value from source, or uses a default if source value is
     * null.
     *
     * @param setter the setter on the target entity
     * @param getter the getter from the source entity
     * @param defaultValue the default value to use if source value is null
     * @param <V> the value type
     * @return this updater for chaining
     */
    public <V> EntityUpdater<S> updateWithDefault(
            Consumer<V> setter, Function<S, V> getter, V defaultValue) {
        V value = getter.apply(source);
        setter.accept(value != null ? value : defaultValue);
        return this;
    }

    /**
     * Updates the target field with the value from source, or uses a supplier for the default if
     * source value is null.
     *
     * @param setter the setter on the target entity
     * @param getter the getter from the source entity
     * @param defaultSupplier supplies the default value if source value is null
     * @param <V> the value type
     * @return this updater for chaining
     */
    public <V> EntityUpdater<S> updateWithDefault(
            Consumer<V> setter, Function<S, V> getter, Supplier<V> defaultSupplier) {
        V value = getter.apply(source);
        setter.accept(value != null ? value : defaultSupplier.get());
        return this;
    }

    /**
     * Updates the target field only if the source value is not null.
     *
     * @param setter the setter on the target entity
     * @param getter the getter from the source entity
     * @param <V> the value type
     * @return this updater for chaining
     */
    public <V> EntityUpdater<S> updateIfNotNull(Consumer<V> setter, Function<S, V> getter) {
        V value = getter.apply(source);
        if (value != null) {
            setter.accept(value);
        }
        return this;
    }

    /**
     * Checks if a field has changed between two values.
     *
     * @param oldValue the old value
     * @param newValue the new value
     * @param <V> the value type
     * @return true if the values are different
     */
    public static <V> boolean hasChanged(V oldValue, V newValue) {
        return !Objects.equals(oldValue, newValue);
    }

    /**
     * Gets the source entity.
     *
     * @return the source entity
     */
    public S getSource() {
        return source;
    }
}
