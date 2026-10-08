package com.leclowndu93150.thaumaturge.content.entity.projectile;

import com.leclowndu93150.thaumaturge.content.particle.FireMoteParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ChargeTrail(int color, float shade, float alpha, float scale) {
    private static final int PUFFS_PER_TICK = 4;
    private static final double PUFF_DRIFT = 0.008;
    private static final int SPARK_COLOR = 0xFFF4E8;
    private static final float SPARK_ALPHA = 0.8F;
    private static final float SPARK_SCALE = 0.25F;
    private static final int SPARK_AGE = 6;
    private static final double SPARK_SCATTER = 0.15;

    public void emit(Entity charge) {
        Level level = charge.level();
        RandomSource random = charge.getRandom();
        Vec3 from = new Vec3(charge.xo, charge.yo, charge.zo);
        Vec3 to = charge.position();
        double lift = charge.getBbHeight() * 0.5;
        for (int puff = 0; puff < PUFFS_PER_TICK; puff++) {
            Vec3 at = from.lerp(to, random.nextDouble());
            float tone = 1.0F - random.nextFloat() * shade;
            level.addParticle(
                    new FireMoteParticleOptions(
                            drift(random),
                            drift(random),
                            drift(random),
                            channel(ARGB32.red(color), tone),
                            channel(ARGB32.green(color), tone),
                            channel(ARGB32.blue(color), tone),
                            alpha,
                            scale,
                            true),
                    at.x,
                    at.y + lift,
                    at.z,
                    0.0,
                    0.0,
                    0.0);
        }
        level.addParticle(
                new ShieldSparkParticleOptions(SPARK_COLOR, SPARK_ALPHA, SPARK_SCALE, SPARK_AGE, 0, false),
                to.x + random.triangle(0.0, SPARK_SCATTER),
                to.y + lift + random.triangle(0.0, SPARK_SCATTER),
                to.z + random.triangle(0.0, SPARK_SCATTER),
                0.0,
                0.0,
                0.0);
    }

    private static double drift(RandomSource random) {
        return random.triangle(0.0, PUFF_DRIFT);
    }

    private static float channel(int value, float tone) {
        return Mth.clamp(value / 255.0F * tone, 0.0F, 1.0F);
    }
}
