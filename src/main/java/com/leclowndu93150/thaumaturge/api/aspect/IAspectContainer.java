package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraft.core.Holder;

/**
 * Something that stores aspects: jars, nodes, alembics, mirrors.
 *
 * <p>Blocks expose it through {@link AspectCapabilities#CONTAINER}. Aspects are passed as {@code Holder<IAspect>} so references survive
 * datapack reloads. Implementations own their persistence and client sync; every mutating call must mark the block entity changed and
 * sync it when the change is visible.
 *
 * @since 1.0.0
 */
public interface IAspectContainer {
    /**
     * @return the stored aspects; never null. Callers must not mutate the returned list.
     */
    AspectList getAspects();

    /**
     * Replaces the stored aspects.
     *
     * @param aspects the new contents, never null
     */
    void setAspects(AspectList aspects);

    /**
     * @param aspect the aspect to test
     * @return whether the container may store the aspect at all; capacity is not considered
     */
    boolean accepts(Holder<IAspect> aspect);

    /**
     * Adds an aspect, as much as fits.
     *
     * @param aspect the aspect to add
     * @param amount the most to add
     * @return the amount that did not fit; 0 when everything was stored
     */
    int fill(Holder<IAspect> aspect, int amount);

    /**
     * Removes an aspect, all or nothing.
     *
     * @param aspect the aspect to remove
     * @param amount the amount to remove
     * @return whether the full amount was there and was removed; nothing is removed otherwise
     */
    boolean drain(Holder<IAspect> aspect, int amount);

    /**
     * @param aspect the aspect to count
     * @return how much of the aspect is stored, never negative
     */
    default int amountOf(Holder<IAspect> aspect) {
        return getAspects().amountOf(aspect);
    }

    /**
     * @param aspect the aspect to count
     * @param amount the amount required
     * @return whether at least that much of the aspect is stored
     */
    default boolean holds(Holder<IAspect> aspect, int amount) {
        return amountOf(aspect) >= amount;
    }
}
