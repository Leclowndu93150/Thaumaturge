package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class WarpChampionTrait extends AbstractChampionTrait {
    private static final float PROC_CHANCE = 0.33F;
    private static final int MIN_WARP = 1;
    private static final int WARP_SPREAD = 3;

    @Override
    public float onAttack(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (target instanceof ServerPlayer player && mob.getRandom().nextFloat() < PROC_CHANCE) {
            WarpHelper.addWarp(player, MIN_WARP + mob.getRandom().nextInt(WARP_SPREAD), WarpType.TEMPORARY);
        }
        return amount;
    }
}
