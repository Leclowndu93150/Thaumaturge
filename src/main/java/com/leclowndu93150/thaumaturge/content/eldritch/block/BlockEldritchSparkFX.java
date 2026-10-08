package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class BlockEldritchSparkFX {
    private BlockEldritchSparkFX() {}

    public static void spawnShockSpark(Level level, BlockPos pos, RandomSource random) {
        SparkParticleOptions data = new SparkParticleOptions(
                ARGB32.color(
                        (int) ((1.0F) * 255.0F),
                        (int) ((0.65F + random.nextFloat() * 0.1F) * 255.0F),
                        (int) ((1.0F) * 255.0F),
                        (int) ((1.0F) * 255.0F)),
                0.8F,
                0.5F);
        level.addParticle(
                data,
                pos.getX() + random.nextFloat(),
                pos.getY() + random.nextFloat(),
                pos.getZ() + random.nextFloat(),
                0.0,
                0.0,
                0.0);
    }
}
