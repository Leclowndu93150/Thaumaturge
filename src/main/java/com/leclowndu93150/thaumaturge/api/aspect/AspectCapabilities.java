package com.leclowndu93150.thaumaturge.api.aspect;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

/**
 * Registry-style holder for the aspect capability types.
 *
 * <p>{@link #CONTAINER} is the block capability for {@link IAspectContainer}. Directional
 * consumers pass a side, while essentia source discovery for the infusion matrix and similar
 * devices queries with a {@code null} side, so a provider meant to feed them must answer the
 * null-side query as well.
 *
 * @since 1.0.0
 */
public final class AspectCapabilities {
    /** Sided block capability for aspect storing. */
    public static final BlockCapability<IAspectContainer, Direction> CONTAINER = BlockCapability.createSided(TCIds.rl("aspect_container"),
            IAspectContainer.class);

    private AspectCapabilities() {}
}
