package com.leclowndu93150.thaumaturge.api.research.scan;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

record PredicateScan(
        ResourceLocation research, BiPredicate<Player, ScanTarget> test, BiConsumer<Player, ScanTarget> effect)
        implements IScannable {
    static final BiConsumer<Player, ScanTarget> NO_EFFECT = (player, target) -> {};

    @Override
    public boolean matches(Player player, ScanTarget target) {
        return test.test(player, target);
    }

    @Override
    public ResourceLocation research(Player player, ScanTarget target) {
        return research;
    }

    @Override
    public void onScanned(Player player, ScanTarget target) {
        effect.accept(player, target);
    }
}
