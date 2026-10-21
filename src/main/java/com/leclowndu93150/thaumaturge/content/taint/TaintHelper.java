package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.api.taint.ITaintOrigin;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.content.taint.spread.FibreGrowth;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSprouting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintHelper {
    private TaintHelper() {}

    public static boolean isTaintBlock(BlockState state) {
        return state.getBlock() instanceof ITaintBlock;
    }

    public static boolean hasSturdyNeighbour(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (facesSturdily(level, pos, direction)) {
                return true;
            }
        }
        return false;
    }

    public static boolean facesSturdily(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighbour = pos.relative(direction);
        return level.getBlockState(neighbour).isFaceSturdy(level, neighbour, direction.getOpposite());
    }

    public static boolean isRootless(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbour);
            if (!(neighbourState.getBlock() instanceof ITaintBlock)
                    && neighbourState.isFaceSturdy(level, neighbour, direction.getOpposite())) {
                return false;
            }
        }
        return true;
    }

    public static int countAdjacentTaint(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof ITaintBlock) {
                count++;
            }
        }
        return count;
    }

    public static boolean placeFibreFromFlux(ServerLevel level, BlockPos pos) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || !ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get()
                || TaintBlooms.isProtected(level, pos)) {
            return false;
        }
        return level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
    }

    public static boolean attemptFibreGrowth(ServerLevel level, BlockPos origin, boolean force) {
        return FibreGrowth.attempt(level, origin, force);
    }

    public static boolean growFromOrigin(ServerLevel level, BlockPos at, ITaintOrigin origin) {
        return FibreGrowth.growFromOrigin(level, at, origin);
    }

    public static boolean canHostFoothold(BlockState state) {
        return PhysicalFlux.isPhysicalFlux(state)
                || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    public static void establishFoothold(ServerLevel level, BlockPos pos, float pressure, int spreadAttempts) {
        if (canHostFoothold(level.getBlockState(pos))) {
            level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
        }
        TaintEcology.addPressure(level, pos, pressure);
        for (int attempt = 0; attempt < spreadAttempts; attempt++) {
            attemptFibreGrowth(level, pos, true);
        }
    }

    public static boolean trySpawnSatelliteSeed(ServerLevel level, BlockPos origin, RandomSource random) {
        return TaintSprouting.trySatellite(level, origin, random);
    }
}
