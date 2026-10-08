package com.leclowndu93150.thaumaturge.content.spell.fx;

import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.CrackShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.EarthPebbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FrostFlakeParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.RiftShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SpellFxStyles {
    private static final int CRACK_VARIANTS = 4;
    private static final int MOTE_AGE = 14;
    private static final int SPARKLE_AGE = 6;

    private SpellFxStyles() {}

    public static void sparkle(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(
                level,
                new SparkleParticleOptions(
                        ARGB32.color(255, color),
                        0.7F + random.nextFloat() * 0.4F,
                        0,
                        1.0F,
                        0.0F,
                        SPARKLE_AGE + random.nextInt(4),
                        true),
                at,
                motion);
    }

    public static void mote(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(
                level,
                new WispyMoteParticleOptions(
                        ARGB32.color(255, color),
                        MOTE_AGE + random.nextInt(6),
                        0.0F,
                        WispyMoteParticleOptions.NO_ENTITY,
                        true),
                at,
                motion);
    }

    public static void flame(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(
                level,
                new FlameFanParticleOptions((float) (1.4 + random.nextGaussian() * 0.2), -0.2F, 0.7F),
                at,
                Vec3.ZERO);
    }

    public static void frost(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(level, new FrostFlakeParticleOptions((float) (0.7 + random.nextGaussian() * 0.25)), at, Vec3.ZERO);
    }

    public static void gust(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(level, new AirGustParticleOptions((float) (1.8 + random.nextGaussian() * 0.4)), at, Vec3.ZERO);
    }

    public static void pebble(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(level, new EarthPebbleParticleOptions((float) (1.0 + random.nextGaussian() * 0.2)), at, Vec3.ZERO);
    }

    public static void flux(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        float shade = 0.3F + random.nextFloat() * 0.25F;
        add(
                level,
                new FluxSwirlParticleOptions(
                        ARGB32.color((int) ((1.0F) * 255.0F), (int) ((shade) * 255.0F), (int) ((0.0F) * 255.0F), (int)
                                ((shade) * 255.0F)),
                        1.8F + random.nextFloat(),
                        0.2F + random.nextFloat() * 0.3F),
                at,
                Vec3.ZERO);
    }

    public static void heal(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(level, TTParticles.HEAL_FLASH.get(), at, Vec3.ZERO);
    }

    public static void curse(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(level, TTParticles.CURSE_SMOKE.get(), at, Vec3.ZERO);
    }

    public static void crack(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(
                level,
                new CrackShardParticleOptions(
                        0xFFFFFF,
                        random.nextInt(CRACK_VARIANTS),
                        (float) (1.6 + random.nextGaussian() * 0.3),
                        6 + random.nextInt(6)),
                at,
                Vec3.ZERO);
    }

    public static void rift(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(level, new RiftShardParticleOptions((float) (0.7 + random.nextGaussian() * 0.25)), at, Vec3.ZERO);
    }

    public static void primal(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(level, TTParticles.PRIMAL_FLARE.get(), at, Vec3.ZERO);
    }

    public static void ward(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(
                level,
                new ShieldSparkParticleOptions(
                        ARGB32.color(255, color),
                        0.9F,
                        0.6F + random.nextFloat() * 0.4F,
                        6 + random.nextInt(6),
                        random.nextInt(6),
                        true),
                at,
                motion);
    }

    public static void bubble(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(
                level,
                new BubbleParticleOptions(
                        ARGB32.color(255, color),
                        0.9F,
                        0.3F + random.nextFloat() * 0.3F,
                        14 + random.nextInt(8),
                        0.01F,
                        false),
                at,
                motion);
    }

    public static void spark(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(
                level,
                new SparkParticleOptions(ARGB32.color(255, color), 0.9F, 0.3F + random.nextFloat() * 0.2F),
                at,
                Vec3.ZERO);
    }

    public static void leaf(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(level, TTParticles.colorOf(TTParticles.LEAF_MOTE, color), at, motion);
    }

    public static void smoke(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        add(level, ParticleTypes.LARGE_SMOKE, at, motion.scale(0.5));
    }

    private static void add(Level level, ParticleOptions options, Vec3 at, Vec3 motion) {
        level.addParticle(options, at.x, at.y, at.z, motion.x, motion.y, motion.z);
    }
}
