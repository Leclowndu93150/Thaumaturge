package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.resources.Identifier;

/**
 * Identifiers of research entries that gameplay systems reference directly.
 *
 * @since 1.0.0
 */
public final class TCResearchEntries {
    /** Unlocks celestial observation through the thaumometer. */
    public static final Identifier CELESTIAL_SCANNING = TCIds.rl("celestial_scanning");

    /** Granted when the thaumometer detects dangerous flux levels in the local aura. */
    public static final Identifier FLUX = TCIds.rl("flux");

    private TCResearchEntries() {}
}
