package com.leclowndu93150.thaumaturge.content.infusion;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;

public record MatrixEnvironment(List<BlockPos> pedestals, int cycleTime, float costMult, float stabilityReplenish) {
    private static final int SCAN_RADIUS = 8;
    private static final int SCAN_UP = 3;
    private static final int SCAN_DOWN = 7;
    private static final int BASE_CYCLE_TIME = 10;
    private static final int PILLAR_DEPTH = 2;
    private static final int UPGRADE_DEPTH = 3;
    private static final int[][] CORNERS = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};

    public static MatrixEnvironment survey(Level level, BlockPos matrixPos) {
        Totals totals =
                new Totals(InfusionStabilitySurvey.survey(level, matrixPos).stabilityReplenish());
        List<BlockPos> pedestals = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -SCAN_RADIUS; xx <= SCAN_RADIUS; xx++) {
            for (int zz = -SCAN_RADIUS; zz <= SCAN_RADIUS; zz++) {
                if (xx == 0 && zz == 0) {
                    continue;
                }
                for (int yy = -SCAN_UP; yy <= SCAN_DOWN; yy++) {
                    cursor.set(matrixPos.getX() + xx, matrixPos.getY() - yy, matrixPos.getZ() + zz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.getBlock() instanceof BlockPedestal) {
                        pedestals.add(cursor.immutable());
                        totals.apply(modifier(state, InfusionDataMaps.PEDESTAL));
                    }
                }
            }
        }
        Block pillars = matchingPillars(level, matrixPos);
        if (pillars != null) {
            totals.apply(modifier(pillars.defaultBlockState(), InfusionDataMaps.PILLAR_SET));
        }
        for (int[] corner : CORNERS) {
            totals.apply(modifier(
                    level.getBlockState(matrixPos.offset(corner[0], -UPGRADE_DEPTH, corner[1])),
                    InfusionDataMaps.MATRIX_UPGRADE));
        }
        return new MatrixEnvironment(
                List.copyOf(pedestals), totals.cycleTime, totals.costMult, totals.stabilityReplenish);
    }

    private static @Nullable InfusionModifier modifier(BlockState state, DataMapType<Block, InfusionModifier> map) {
        return state.getBlock().builtInRegistryHolder().getData(map);
    }

    private static @Nullable Block matchingPillars(Level level, BlockPos matrixPos) {
        Block first = null;
        for (int[] corner : CORNERS) {
            Block block = level.getBlockState(matrixPos.offset(corner[0], -PILLAR_DEPTH, corner[1]))
                    .getBlock();
            if (!(block instanceof BlockPillar) || (first != null && block != first)) {
                return null;
            }
            first = block;
        }
        return first;
    }

    public static boolean validLocation(Level level, BlockPos matrixPos) {
        if (!(level.getBlockState(matrixPos.below(PILLAR_DEPTH)).getBlock() instanceof BlockPedestal)) {
            return false;
        }
        for (int[] corner : CORNERS) {
            if (!(level.getBlockState(matrixPos.offset(corner[0], -PILLAR_DEPTH, corner[1]))
                            .getBlock()
                    instanceof BlockPillar)) {
                return false;
            }
        }
        return true;
    }

    private static final class Totals {
        private int cycleTime = BASE_CYCLE_TIME;
        private float costMult = 1.0F;
        private float stabilityReplenish;

        private Totals(float stabilityReplenish) {
            this.stabilityReplenish = stabilityReplenish;
        }

        private void apply(@Nullable InfusionModifier modifier) {
            if (modifier == null) {
                return;
            }
            cycleTime += modifier.cycleTime();
            costMult += modifier.cost();
            stabilityReplenish += modifier.stability();
        }
    }
}
