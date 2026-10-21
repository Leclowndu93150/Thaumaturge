package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;

/**
 * An aspect orb's source value in whole vis, before pickup modifiers.
 * Eligibility describes the player and orb at inspection time, not a reservation or a promise
 * that a receiving wand has room. One vis is one hundred centivis.
 */
public record AspectOrbSnapshot(ResourceKey<IAspect> aspect, int vis, boolean eligible) {
}
