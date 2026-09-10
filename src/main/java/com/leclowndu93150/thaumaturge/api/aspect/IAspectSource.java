package com.leclowndu93150.thaumaturge.api.aspect;

/**
 * An aspect container that may supply or receive essentia for infusion-style devices.
 *
 * <p>Expose this interface through {@link AspectCapabilities#CONTAINER}. Native source discovery
 * queries that capability with a {@code null} side, so providers intended for infusion must return
 * a view for null context. Discovery considers only already-loaded positions, orders candidates by
 * distance, and never force-loads chunks.
 *
 * <p>The default native search covers a cube extending 12 blocks from its center. Directional
 * searches cover the same 25-by-25 cross-section and extend 12 blocks forward. A failed search is
 * retried after 200 server ticks. Sources transfer one aspect unit per successful operation through
 * the inherited {@link IAspectContainer} methods.
 *
 * <p>Implementations own their mutation, persistence and synchronization behavior. Capability
 * instances follow NeoForge block-capability invalidation rules and must not be retained after
 * invalidation.
 *
 * @since 1.0.0
 */
public interface IAspectSource extends IAspectContainer {

    /**
     * Returns whether automated source discovery should temporarily skip this container.
     *
     * @return {@code true} to prevent both native insertion and extraction
     */
    boolean isBlocked();
}
