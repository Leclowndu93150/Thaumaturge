package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.Optional;
import net.minecraft.world.entity.player.Player;

/** Server-evaluated state of a recipe's research requirement. */
public enum ResearchStatus {
    NOT_REQUIRED,
    UNLOCKED,
    LOCKED,
    INVALID;

    public static ResearchStatus evaluate(Player player, Optional<ResearchGate> gate) {
        if (player == null || gate == null) {
            return INVALID;
        }
        if (gate.isEmpty()) {
            return NOT_REQUIRED;
        }
        return ResearchGate.passes(player, gate.get()) ? UNLOCKED : LOCKED;
    }

    public boolean permitsCrafting() {
        return this == NOT_REQUIRED || this == UNLOCKED;
    }
}
