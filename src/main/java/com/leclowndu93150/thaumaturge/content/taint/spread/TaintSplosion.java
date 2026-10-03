package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

public final class TaintSplosion {
    private static final int ATTEMPTS = 10;
    private static final float PRESSURE = 0.01F;

    private TaintSplosion() {}

    public static void burstOnSurface(ServerLevel level, BlockPos center, RandomSource random, float spread) {
        burst(level, center, random, spread, true);
    }

    public static void burstAtHeight(ServerLevel level, BlockPos center, RandomSource random, float spread) {
        burst(level, center, random, spread, false);
    }

    private static void burst(
            ServerLevel level, BlockPos center, RandomSource random, float spread, boolean onSurface) {
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int x = center.getX() + (int) ((random.nextFloat() - random.nextFloat()) * spread);
            int z = center.getZ() + (int) ((random.nextFloat() - random.nextFloat()) * spread);
            if (!random.nextBoolean()) {
                continue;
            }
            int y = onSurface ? level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) : center.getY();
            BlockPos column = new BlockPos(x, y, z);
            if (!level.hasChunk(column.getX() >> 4, column.getZ() >> 4)
                    || !TaintBiomeManager.taintColumn(level, column)) {
                continue;
            }
            if (level.getBlockState(column).canBeReplaced()
                    && level.getBlockState(column).getFluidState().isEmpty()
                    && BlockTaintFibre.hasSolidAttachment(level, column)) {
                level.setBlock(column, BlockTaintFibre.stateForWorld(level, column), Block.UPDATE_ALL);
            }
            TaintEcology.addPressure(level, column, PRESSURE);
        }
    }
}
