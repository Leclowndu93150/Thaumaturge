package com.leclowndu93150.thaumaturge.api.aura;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

/**
 * Holder for the vis relay block capabilities.
 *
 * @since 1.0.0
 */
public final class VisRelayCapabilities {
    /**
     * Block capability for a vis relay network source. It has no context: relays query it without
     * a side.
     */
    public static final BlockCapability<IVisRelaySource, @Nullable Void> SOURCE = BlockCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "vis_relay_source"), IVisRelaySource.class);

    private VisRelayCapabilities() {}
}
