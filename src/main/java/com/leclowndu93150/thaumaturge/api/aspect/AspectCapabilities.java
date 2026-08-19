package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;

/**
 * Registry-style holder for the aspect capability types.
 *
 * <p>{@link #CONTAINER} is the block capability for {@link IAspectContainer}; it requires
 * side context.
 *
 * @since 1.0.0
 */
public final class AspectCapabilities {
    /** Sided block capability for aspect storing. */
    public static final BlockCapability<IAspectContainer, Direction> CONTAINER = BlockCapability.createSided(Identifier.fromNamespaceAndPath("thaumaturge", "aspect_container"),
            IAspectContainer.class);

    private AspectCapabilities() {}
}
