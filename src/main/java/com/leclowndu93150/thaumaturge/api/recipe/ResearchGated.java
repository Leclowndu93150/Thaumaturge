package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.Optional;
import net.minecraft.world.entity.player.Player;

public interface ResearchGated {

    Optional<ResearchGate> researchGate();

    default boolean doesPassGate(Player player) {
        return ResearchGate.passes(player, researchGate().orElse(null));
    }

    /**
     * Evaluates this recipe's research gate for a player.
     *
     * @param player the player
     * @return whether the recipe is ungated, unlocked or locked for the player
     * @since 1.0.0
     */
    default ResearchGateStatus gateStatus(Player player) {
        return ResearchGateStatus.of(player, researchGate());
    }
}
