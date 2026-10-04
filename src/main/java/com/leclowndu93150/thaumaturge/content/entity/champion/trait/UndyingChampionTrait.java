package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import net.minecraft.world.entity.LivingEntity;

public final class UndyingChampionTrait extends AbstractChampionTrait {
    private static final int HEAL_INTERVAL_TICKS = 20;
    private static final float HEAL_AMOUNT = 1.0F;

    @Override
    public void tick(LivingEntity mob) {
        if (mob.tickCount % HEAL_INTERVAL_TICKS == 0) {
            mob.heal(HEAL_AMOUNT);
        }
    }
}
