package com.leclowndu93150.thaumaturge.api.spell.cast;

import net.minecraft.resources.Identifier;

/**
 * A named number carried down a spell tree in {@link SpellState}. Modifiers write stats, later
 * nodes read them. Stats are open: an addon modifier and an addon effect can share their own.
 *
 * @param id           the stat id
 * @param defaultValue the value when no node wrote the stat
 * @since 1.0.0
 */
public record SpellStat(Identifier id, float defaultValue) {
}
