package com.leclowndu93150.thaumaturge.content.aura.pressure;

import com.leclowndu93150.thaumaturge.content.warp.WarpManager;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class ExhaustPressureEvent extends AbstractFluxPressureEvent {
    private static final String NAME = "exhaust";
    private static final int WEIGHT = 5;
    private static final float COST = 15.0F;
    private static final boolean ALLOWED_NEAR_TAINT = true;
    private static final double RANGE = 16.0;
    private static final int DURATION = 3000;
    private static final int AMPLIFIER = 2;
    private static final String MESSAGE = "warp.thaumaturge.fluxevent.2";

    public ExhaustPressureEvent() {
        super(NAME, WEIGHT, COST, ALLOWED_NEAR_TAINT);
    }

    @Override
    public boolean fire(ServerLevel level, BlockPos origin, FluxPressureState state) {
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(RANGE));
        if (targets.isEmpty()) {
            return false;
        }
        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayer player) {
                WarpManager.sendActionBar(player, MESSAGE);
            }
            target.addEffect(new MobEffectInstance(TTMobEffects.INFECTIOUS_VIS_EXHAUST, DURATION, AMPLIFIER, false, true, false));
        }
        return true;
    }
}
