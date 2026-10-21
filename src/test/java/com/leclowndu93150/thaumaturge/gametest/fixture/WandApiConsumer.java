package com.leclowndu93150.thaumaturge.gametest.fixture;

import com.leclowndu93150.thaumaturge.api.wands.AspectOrbAccess;
import com.leclowndu93150.thaumaturge.api.wands.AspectOrbPickupEvent;
import com.leclowndu93150.thaumaturge.api.wands.AspectOrbSnapshot;
import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.api.wands.WandSnapshot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class WandApiConsumer {
    private WandApiConsumer() {}

    public static int collect(Entity candidate, ServerPlayer player) {
        return AspectOrbAccess.inspect(candidate, player).filter(AspectOrbSnapshot::eligible).isPresent() ? AspectOrbAccess.collect(candidate, player) : 0;
    }

    public static void applyBonus(AspectOrbPickupEvent event, ResourceKey<WandRod> rod, WandSnapshot.Variant variant, int bonusCentivis) {
        if (bonusCentivis < 0) {
            throw new IllegalArgumentException("Bonus must be nonnegative");
        }
        if (WandAccess.inspect(event.getWand()).filter(snapshot -> snapshot.rod().equals(rod) && snapshot.variant() == variant).isEmpty()) {
            return;
        }
        int capacity = WandAccess.capacityCentivis(event.getWand(), event.getOrb().aspect()).orElse(0);
        long credit = (long) event.getCreditCentivis() + bonusCentivis;
        event.setCreditCentivis((int) Math.min(credit, capacity));
    }
}
