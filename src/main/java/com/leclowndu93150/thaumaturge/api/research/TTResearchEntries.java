package com.leclowndu93150.thaumaturge.api.research;

import net.minecraft.resources.ResourceLocation;

/**
 * Identifiers of research entries that gameplay systems reference directly.
 *
 * @since 1.0.0
 */
public final class TTResearchEntries {
    /** Unlocks celestial observation through the thaumometer. */
    public static final ResourceLocation CELESTIAL_SCANNING = id("celestial_scanning");

    /** Granted when the thaumometer detects dangerous flux levels in the local aura. */
    public static final ResourceLocation FLUX = id("flux");

    private TTResearchEntries() {}

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("thaumaturge", path);
    }
}
