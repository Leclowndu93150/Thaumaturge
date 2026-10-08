package com.leclowndu93150.thaumaturge.content.taint.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class VisExhaustEffect extends MobEffect {
    public VisExhaustEffect() {
        super(MobEffectCategory.HARMFUL, 0x80407F);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return false;
    }

    @Override
    public boolean applyEffectTick(LivingEntity mob, int amplification) {
        if (!(mob.level() instanceof ServerLevel level)) return true;
        return true;
    }
}
