package com.leclowndu93150.thaumaturge.content.taint.effect;

import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class InfectiousVisExhaustEffect extends MobEffect {
    private static final double SPREAD_RADIUS = 4.0;
    private static final int SPREAD_INTERVAL = 40;
    private static final int EFFECT_DURATION = 6000;

    public InfectiousVisExhaustEffect() {
        super(MobEffectCategory.HARMFUL, 0x60306F);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % SPREAD_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity mob, int amplification) {
        if (mob.level().isClientSide()) {
            return true;
        }

        ServerLevel level = (ServerLevel) mob.level();
        AABB box = mob.getBoundingBox().inflate(SPREAD_RADIUS, SPREAD_RADIUS, SPREAD_RADIUS);
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                box,
                target -> target != mob && !target.hasEffect(TCMobEffects.INFECTIOUS_VIS_EXHAUST));
        for (LivingEntity target : nearby) {
            if (amplification > 0) {
                target.addEffect(new MobEffectInstance(
                        TCMobEffects.INFECTIOUS_VIS_EXHAUST, EFFECT_DURATION, amplification - 1, false, true, false));
            } else {
                target.addEffect(
                        new MobEffectInstance(TCMobEffects.VIS_EXHAUST, EFFECT_DURATION, 0, false, true, false));
            }
        }
        return true;
    }
}
