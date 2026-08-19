package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.infusion.IInfusionStabiliser;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class InfusionStabilitySurvey {
    private static final int HORIZONTAL_RANGE = 8;
    private static final int RANGE_ABOVE = 3;
    private static final int RANGE_BELOW = 7;
    private static final double DIMINISHING_FACTOR = 0.75;

    private InfusionStabilitySurvey() {}

    public record Result(float stabilityReplenish, List<BlockPos> problemBlocks) {
    }

    public static Result survey(Level level, BlockPos matrix) {
        Set<Long> stabilisers = new LinkedHashSet<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -HORIZONTAL_RANGE; xx <= HORIZONTAL_RANGE; xx++) {
            for (int zz = -HORIZONTAL_RANGE; zz <= HORIZONTAL_RANGE; zz++) {
                if (xx == 0 && zz == 0) {
                    continue;
                }
                for (int yy = -RANGE_ABOVE; yy <= RANGE_BELOW; yy++) {
                    cursor.set(matrix.getX() + xx, matrix.getY() - yy, matrix.getZ() + zz);
                    if (isStabiliser(level, cursor)) {
                        stabilisers.add(cursor.asLong());
                    }
                }
            }
        }

        float replenish = 0.0F;
        List<BlockPos> problems = new ArrayList<>();
        Map<Block, Integer> countedByType = new HashMap<>();
        while (!stabilisers.isEmpty()) {
            long first = stabilisers.iterator().next();
            stabilisers.remove(first);
            BlockPos pos = BlockPos.of(first);
            BlockPos mirrored = new BlockPos(2 * matrix.getX() - pos.getX(), pos.getY(), 2 * matrix.getZ() - pos.getZ());
            stabilisers.remove(mirrored.asLong());

            Block block = level.getBlockState(pos).getBlock();
            Block mirroredBlock = level.getBlockState(mirrored).getBlock();
            float amount = stabilizationAmount(level, pos);
            float mirroredAmount = stabilizationAmount(level, mirrored);
            if (block == mirroredBlock && amount == mirroredAmount) {
                if (block instanceof IInfusionStabiliser stabiliser && stabiliser.hasSymmetryPenalty(level, pos, mirrored)) {
                    replenish -= stabiliser.getSymmetryPenalty(level, pos);
                    problems.add(pos);
                } else {
                    replenish += diminishingReturns(countedByType, block, amount);
                }
            } else {
                replenish -= Math.max(amount, mirroredAmount);
                problems.add(pos);
            }
        }
        return new Result(replenish, List.copyOf(problems));
    }

    public static boolean isStabiliser(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(TCBlockTags.INFUSION_STABILISERS)) {
            return true;
        }
        return state.getBlock() instanceof IInfusionStabiliser stabiliser && stabiliser.canStabiliseInfusion(level, pos);
    }

    private static float stabilizationAmount(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof IInfusionStabiliser stabiliser ? stabiliser.getStabilizationAmount(level, pos) : IInfusionStabiliser.DEFAULT_STABILIZATION;
    }

    private static float diminishingReturns(Map<Block, Integer> countedByType, Block block, float base) {
        int counted = countedByType.merge(block, 1, Integer::sum) - 1;
        if (counted <= 0) {
            return base;
        }
        return (float) (base * Math.pow(DIMINISHING_FACTOR, counted));
    }
}
