package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemPartAbility;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class GolemLegLevitator implements IGolemPartAbility {
    private static final int GROUNDED_PULSE = 5;
    private static final double EXHAUST_HEIGHT = 0.1;
    private static final double EXHAUST_FALL = -0.1;
    private static final double EXHAUST_SPREAD = 100.0;

    @Override
    public void tick(IGolemAPI golem) {
        Level level = golem.level();
        LivingEntity body = golem.asEntity();
        boolean idle = body.onGround() && body.tickCount % GROUNDED_PULSE != 0;
        if (!level.isClientSide() || idle) {
            return;
        }
        RandomSource random = level.getRandom();
        level.addParticle(
                TTParticles.GOLEM_TRAIL.get(),
                body.getX(),
                body.getY() + EXHAUST_HEIGHT,
                body.getZ(),
                random.nextGaussian() / EXHAUST_SPREAD,
                EXHAUST_FALL,
                random.nextGaussian() / EXHAUST_SPREAD);
    }
}
