package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class TaintSplosion {
    private static final int ATTEMPTS = 10;
    private static final float SPOT_PRESSURE = 0.01F;

    private TaintSplosion() {}

    public static void burstOnSurface(ServerLevel level, BlockPos center, RandomSource random, float spread) {
        burst(level, center, random, spread, true);
    }

    public static void burstAtHeight(ServerLevel level, BlockPos center, RandomSource random, float spread) {
        burst(level, center, random, spread, false);
    }

    private static void burst(ServerLevel level, BlockPos center, RandomSource random, float spread, boolean onSurface) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int dx = centreWeighted(random, spread);
            int dz = centreWeighted(random, spread);
            if (!random.nextBoolean()) {
                continue;
            }
            BlockPos column = center.offset(dx, 0, dz);
            if (!level.hasChunkAt(column)) {
                continue;
            }
            BlockPos spot = onSurface ? level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column) : column;
            if (canTaintColumn(level, spot)) {
                seed(level, spot);
            }
        }
    }

    private static int centreWeighted(RandomSource random, float spread) {
        return (int) ((random.nextFloat() - random.nextFloat()) * spread);
    }

    private static boolean canTaintColumn(ServerLevel level, BlockPos spot) {
        if (TaintBlooms.isProtected(level, spot)) {
            return false;
        }
        return TaintBiomeManager.isTainted(level, spot) || TaintBiomeManager.taintColumn(level, spot);
    }

    private static void seed(ServerLevel level, BlockPos spot) {
        BlockState state = level.getBlockState(spot);
        if ((state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty() && TaintHelper.hasSturdyNeighbour(level, spot)) {
            level.setBlock(spot, BlockTaintFibre.stateForWorld(level, spot), Block.UPDATE_ALL);
        }
        TaintEcology.addPressure(level, spot, SPOT_PRESSURE);
    }
}
