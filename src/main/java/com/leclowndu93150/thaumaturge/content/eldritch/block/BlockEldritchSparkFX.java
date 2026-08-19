package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class BlockEldritchSparkFX {
    private BlockEldritchSparkFX() {}

    public static void spawnShockSpark(Level level, BlockPos pos, RandomSource random) {
        SparkParticleOptions data = new SparkParticleOptions(ARGB.colorFromFloat(1.0F, 0.65F + random.nextFloat() * 0.1F, 1.0F, 1.0F), 0.8F, 0.5F);
        level.addParticle(data, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
    }
}
