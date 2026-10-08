package com.leclowndu93150.thaumaturge.api;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Holds the implementation behind one of the static API facades.
 *
 * <p>Thaumaturge binds every facade exactly once during mod construction. A second bind is rejected so that no addon can
 * replace an implementation, and a call made before binding fails fast instead of returning a silent default.
 *
 * @param <T> the facade's bindings type
 * @since 1.1.0
 */
public final class ApiBinding<T> {
    private final String owner;
    private volatile @Nullable T value;

    /**
     * @param owner the facade name used in error messages
     */
    public ApiBinding(String owner) {
        this.owner = owner;
    }

    /**
     * Installs the implementation.
     *
     * @param impl the implementation; must not be null
     * @throws IllegalStateException when an implementation is already installed
     */
    public void bind(T impl) {
        if (value != null) {
            throw new IllegalStateException(owner + " already bound");
        }
        value = Objects.requireNonNull(impl, owner + " binding");
    }

    /**
     * @return the installed implementation
     * @throws IllegalStateException when nothing has been bound yet
     */
    public T get() {
        T current = value;
        if (current == null) {
            throw new IllegalStateException(owner + " used before Thaumaturge bound it");
        }
        return current;
    }

    /**
     * @return whether an implementation has been installed
     */
    public boolean isBound() {
        return value != null;
    }
}
