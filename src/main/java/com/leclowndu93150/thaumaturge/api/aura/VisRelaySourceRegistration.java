package com.leclowndu93150.thaumaturge.api.aura;

/** Lifecycle handle for one addon vis-relay source registration. */
public interface VisRelaySourceRegistration extends AutoCloseable {
    /** Immediately prevents further linking, simulation, or commitment. */
    void invalidate();

    boolean isValid();

    @Override
    default void close() {
        invalidate();
    }
}
