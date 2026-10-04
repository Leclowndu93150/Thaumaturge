package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class FieryChampionTrait extends AbstractChampionTrait {
    private static final float PROC_CHANCE = 0.4F;
    private static final int FIRE_SECONDS = 4;

    @Override
    public float onAttack(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.getRandom().nextFloat() < PROC_CHANCE) {
            target.igniteForSeconds(FIRE_SECONDS);
        }
        return amount;
    }
}
