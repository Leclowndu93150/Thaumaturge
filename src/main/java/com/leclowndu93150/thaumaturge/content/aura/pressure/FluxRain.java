package com.leclowndu93150.thaumaturge.content.aura.pressure;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public final class FluxRain {
    private static final int RADIUS = 16;
    private static final int PARTICLE_INTERVAL = 5;
    private static final int PARTICLE_COUNT = 6;
    private static final double PARTICLE_Y_OFFSET = 0.25;
    private static final double PARTICLE_SPREAD_XZ = 12.0;
    private static final double PARTICLE_SPREAD_Y = 2.0;
    private static final double PARTICLE_SPEED = 0.02;
    private static final int POOL_INTERVAL = 20;
    private static final float POOL_FLUX_COST = 1.0F;
    private static final float POOL_FLUX_EPSILON = 0.001F;
    private static final int STARVED_TICK_PENALTY = 20;
    private static final float POOL_PRESSURE = 0.02F;

    private final BlockPos center;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private int remainingTicks;

    public FluxRain(BlockPos center, int lifespan) {
        this.center = center.immutable();
        this.remainingTicks = lifespan;
    }

    public BlockPos center() {
        return center;
    }

    public boolean tick(ServerLevel level) {
        if (--remainingTicks <= 0) {
            return false;
        }
        if (remainingTicks % PARTICLE_INTERVAL == 0) {
            level.sendParticles(
                    ParticleTypes.WITCH,
                    center.getX() + 0.5,
                    center.getY() + PARTICLE_Y_OFFSET,
                    center.getZ() + 0.5,
                    PARTICLE_COUNT,
                    PARTICLE_SPREAD_XZ,
                    PARTICLE_SPREAD_Y,
                    PARTICLE_SPREAD_XZ,
                    PARTICLE_SPEED);
        }
        if (remainingTicks % POOL_INTERVAL == 0) {
            pool(level, level.getRandom());
        }
        return true;
    }

    private void pool(ServerLevel level, RandomSource random) {
        cursor.set(
                center.getX() + random.nextInt(RADIUS * 2 + 1) - RADIUS,
                center.getY(),
                center.getZ() + random.nextInt(RADIUS * 2 + 1) - RADIUS);
        if (!level.hasChunkAt(cursor)) {
            return;
        }
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, cursor);
        BlockPos target = level.getBlockState(surface).canBeReplaced() ? surface : surface.above();
        if (TaintHelper.isNearTaintSeed(level, target)) {
            return;
        }
        if (AuraHelper.drainFlux(level, target, POOL_FLUX_COST, true) + POOL_FLUX_EPSILON < POOL_FLUX_COST) {
            remainingTicks = Math.max(0, remainingTicks - STARVED_TICK_PENALTY);
            return;
        }
        if (PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA)) {
            AuraHelper.drainFlux(level, target, POOL_FLUX_COST, false);
            TaintEcology.addPressure(level, target, POOL_PRESSURE);
        }
    }
}
