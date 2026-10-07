package com.leclowndu93150.thaumaturge.content.aura.pressure;

import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class FluxLightning {
    private static final int MIN_DELAY = 4;
    private static final int DELAY_SPREAD = 5;
    private static final float THUNDER_VOLUME = 4.0F;
    private static final float THUNDER_PITCH = 0.9F;
    private static final float IMPACT_VOLUME = 2.0F;
    private static final float IMPACT_PITCH = 0.5F;
    private static final float PITCH_SPREAD = 0.2F;
    private static final int SPARK_COUNT = 24;
    private static final double SPARK_Y_OFFSET = 0.8;
    private static final double SPARK_SPREAD_XZ = 0.5;
    private static final double SPARK_SPREAD_Y = 1.0;
    private static final double SPARK_SPEED = 0.12;
    private static final int SCATTER_ATTEMPTS = 4;
    private static final int SCATTER_RANGE = 5;
    private static final double HIT_RADIUS = 3.0;
    private static final float HIT_DAMAGE = 3.0F;
    private static final int FLUX_TAINT_TICKS = 1200;
    private static final float GOO_PRESSURE = 0.04F;

    private final BlockPos strike;
    private int remainingFlashes;
    private int delay;

    public FluxLightning(BlockPos strike, int remainingFlashes, RandomSource random) {
        this.strike = strike.immutable();
        this.remainingFlashes = remainingFlashes;
        this.delay = nextDelay(random);
    }

    public boolean tick(ServerLevel level) {
        if (--delay > 0) {
            return true;
        }
        flash(level, strike, false);
        if (--remainingFlashes <= 0) {
            return false;
        }
        delay = nextDelay(level.getRandom());
        return true;
    }

    private static int nextDelay(RandomSource random) {
        return MIN_DELAY + random.nextInt(DELAY_SPREAD);
    }

    public static void flash(ServerLevel level, BlockPos strike, boolean scatter) {
        RandomSource random = level.getRandom();
        level.playSound(null, strike, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, THUNDER_VOLUME, THUNDER_PITCH + random.nextFloat() * PITCH_SPREAD);
        level.playSound(null, strike, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, IMPACT_VOLUME, IMPACT_PITCH + random.nextFloat() * PITCH_SPREAD);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, strike.getX() + 0.5, strike.getY() + SPARK_Y_OFFSET, strike.getZ() + 0.5, SPARK_COUNT, SPARK_SPREAD_XZ, SPARK_SPREAD_Y, SPARK_SPREAD_XZ,
                SPARK_SPEED);
        placeGoo(level, strike);
        if (scatter) {
            for (int attempt = 0; attempt < SCATTER_ATTEMPTS; attempt++) {
                placeGoo(level, strike.offset(random.nextInt(SCATTER_RANGE) - SCATTER_RANGE / 2, random.nextInt(SCATTER_RANGE) - SCATTER_RANGE / 2, random.nextInt(SCATTER_RANGE) - SCATTER_RANGE / 2));
            }
        }
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(strike).inflate(HIT_RADIUS))) {
            target.hurtServer(level, level.damageSources().magic(), HIT_DAMAGE);
            target.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, FLUX_TAINT_TICKS, 0, false, true, false));
        }
    }

    private static void placeGoo(ServerLevel level, BlockPos target) {
        if (!level.hasChunkAt(target)) {
            return;
        }
        BlockPos placedAt = target;
        if (!PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA)) {
            placedAt = target.above();
            if (!PhysicalFlux.placeGoo(level, placedAt, PhysicalFlux.MAX_QUANTA)) {
                return;
            }
        }
        TaintEcology.addPressure(level, placedAt, GOO_PRESSURE);
    }
}
