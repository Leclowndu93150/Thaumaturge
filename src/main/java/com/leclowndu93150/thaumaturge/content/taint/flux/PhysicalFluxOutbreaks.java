package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public final class PhysicalFluxOutbreaks {
    private static final float MIN_WEIGHT = 10.0F;
    private static final float CHANCE_DIVISOR = 300.0F;
    private static final float MAX_CHANCE = 0.18F;
    private static final float BASE_PRESSURE = 0.10F;
    private static final float PRESSURE_PER_WEIGHT = 0.0125F;
    private static final float MAX_PRESSURE = 0.30F;
    private static final int INITIAL_SPREAD_ATTEMPTS = 8;
    private static final int SURFACE_SEARCH_DEPTH = 24;

    private PhysicalFluxOutbreaks() {}

    public static boolean isEnabled() {
        return ThaumaturgeCommonConfig.PHYSICAL_FLUX_TAINT_OUTBREAKS.get() && ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get() && !ThaumaturgeCommonConfig.WUSS_MODE.get();
    }

    public static boolean tryOutbreak(ServerLevel level, LevelChunk chunk, RandomSource random) {
        if (!isEnabled()) {
            return false;
        }
        PhysicalFluxSamples samples = chunk.getExistingDataOrNull(TTAttachments.PHYSICAL_FLUX_SAMPLES.get());
        if (samples == null) {
            return false;
        }
        List<BlockPos> pockets = samples.activePositions(chunk);
        float[] weights = new float[pockets.size()];
        float totalWeight = 0.0F;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = taintWeight(chunk.getBlockState(pockets.get(i)));
            totalWeight += weights[i];
        }
        if (totalWeight < MIN_WEIGHT || random.nextFloat() >= Math.min(MAX_CHANCE, (totalWeight - MIN_WEIGHT) / CHANCE_DIVISOR)) {
            return false;
        }
        int first = pick(weights, random.nextFloat() * totalWeight);
        for (int offset = 0; offset < weights.length; offset++) {
            int index = (first + offset) % weights.length;
            if (establish(level, pockets.get(index), weights[index])) {
                return true;
            }
        }
        return false;
    }

    private static float taintWeight(BlockState state) {
        return state.getBlock() instanceof PhysicalFluxBlock flux ? flux.fluxAmount(state) * flux.taintWeightPerQuantum() : 0.0F;
    }

    private static int pick(float[] weights, float roll) {
        float remaining = roll;
        for (int i = 0; i < weights.length; i++) {
            remaining -= weights[i];
            if (remaining <= 0.0F) {
                return i;
            }
        }
        return weights.length - 1;
    }

    private static boolean establish(ServerLevel level, BlockPos source, float weight) {
        BlockState sourceState = level.getBlockState(source);
        if (!(sourceState.getBlock() instanceof PhysicalFluxBlock flux)) {
            return false;
        }
        int cost = Math.min(flux.outbreakCost(), flux.fluxAmount(sourceState));
        BlockPos target = findTarget(level, source);
        if (target == null || !TaintBiomeManager.taintColumn(level, target)) {
            return false;
        }
        TaintHelper.establishFoothold(level, target, Math.min(MAX_PRESSURE, BASE_PRESSURE + weight * PRESSURE_PER_WEIGHT), INITIAL_SPREAD_ATTEMPTS);
        PhysicalFlux.reduce(level, source, cost);
        return true;
    }

    private static @Nullable BlockPos findTarget(ServerLevel level, BlockPos source) {
        if (TaintHelper.isAdjacentToSolidBlock(level, source)) {
            return source;
        }
        BlockPos.MutableBlockPos cursor = source.mutable();
        int minY = Math.max(level.getMinY(), source.getY() - SURFACE_SEARCH_DEPTH);
        while (cursor.getY() > minY) {
            cursor.move(Direction.DOWN);
            BlockState floor = level.getBlockState(cursor);
            BlockPos above = cursor.above();
            if (!floor.isAir() && floor.getFluidState().isEmpty() && !floor.canBeReplaced() && TaintHelper.canHostFoothold(level.getBlockState(above))
                    && TaintHelper.isAdjacentToSolidBlock(level, above)) {
                return above;
            }
        }
        return null;
    }
}
