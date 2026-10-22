package com.leclowndu93150.thaumaturge.content.taint.entity.ability;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class WarpingStrikeTrait implements MobTrait {
    private static final int WARP_PER_HIT = 1;

    @Override
    public float onAttack(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (target instanceof ServerPlayer player) {
            WarpHelper.addWarp(player, WARP_PER_HIT, WarpType.TEMPORARY);
        }
        return amount;
    }
}
