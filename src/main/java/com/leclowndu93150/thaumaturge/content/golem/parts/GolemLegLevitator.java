package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class GolemLegLevitator implements GolemLeg.ILegFunction {
    private static final int PARTICLE_GRID = 16;
    private static final int PARTICLE_START = 56;
    private static final int GROUND_INTERVAL = 5;

    @Override
    public void onUpdateTick(IGolemAPI golem) {
        Level level = golem.getGolemWorld();
        LivingEntity entity = golem.getGolemEntity();
        if (!level.isClientSide() || (entity.onGround() && entity.tickCount % GROUND_INTERVAL != 0)) {
            return;
        }
        RandomSource rand = level.getRandom();
        level.addParticle(TCParticles.GOLEM_TRAIL.get(), entity.getX(), entity.getY() + 0.1, entity.getZ(), rand.nextGaussian() / 100.0, -0.1, rand.nextGaussian() / 100.0);
    }
}
