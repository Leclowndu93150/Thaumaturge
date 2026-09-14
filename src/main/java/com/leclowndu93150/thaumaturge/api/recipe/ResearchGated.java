package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.Optional;
import net.minecraft.world.entity.player.Player;

public interface ResearchGated {

    Optional<ResearchGate> researchGate();

    default boolean doesPassGate(Player player) {
        return ResearchGate.passes(player, researchGate().orElse(null));
    }

    default ResearchStatus researchStatus(Player player) {
        return ResearchStatus.evaluate(player, researchGate());
    }
}
