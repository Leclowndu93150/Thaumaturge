package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.Optional;
import net.minecraft.world.entity.player.Player;

/**
 * Where a player stands against a recipe's research gate. Evaluated on the server, where research
 * knowledge is authoritative.
 *
 * @since 1.0.0
 */
public enum ResearchGateStatus {
    /** The recipe has no research gate. */
    NOT_REQUIRED,
    /** The recipe has a gate and the player passes it. */
    UNLOCKED,
    /** The recipe has a gate and the player does not pass it. */
    LOCKED;

    /**
     * Evaluates a gate for a player.
     *
     * @param player the player
     * @param gate   the recipe's gate, empty when it has none
     * @return the status
     * @throws IllegalStateException when called before the implementation has bound
     *                               {@link ResearchGate}
     */
    public static ResearchGateStatus of(Player player, Optional<ResearchGate> gate) {
        if (gate.isEmpty()) {
            return NOT_REQUIRED;
        }
        return ResearchGate.passes(player, gate.get()) ? UNLOCKED : LOCKED;
    }

    /**
     * Whether the status lets the player craft the recipe.
     *
     * @return true for {@link #NOT_REQUIRED} and {@link #UNLOCKED}
     */
    public boolean permitsCrafting() {
        return this != LOCKED;
    }
}
