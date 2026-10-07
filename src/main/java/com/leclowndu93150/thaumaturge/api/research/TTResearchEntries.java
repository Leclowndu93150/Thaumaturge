package com.leclowndu93150.thaumaturge.api.research;

import net.minecraft.resources.Identifier;

/**
 * Identifiers of research entries that gameplay systems reference directly.
 *
 * @since 1.0.0
 */
public final class TTResearchEntries {
    /** Unlocks celestial observation through the thaumometer. */
    public static final Identifier CELESTIAL_SCANNING = id("celestial_scanning");

    /** Granted when the thaumometer detects dangerous flux levels in the local aura. */
    public static final Identifier FLUX = id("flux");

    private TTResearchEntries() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("thaumaturge", path);
    }
}
