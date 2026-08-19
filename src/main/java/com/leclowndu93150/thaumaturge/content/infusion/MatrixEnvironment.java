package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record MatrixEnvironment(List<BlockPos> pedestals, int cycleTime, float costMult, float stabilityReplenish) {
    private static final int SCAN_RADIUS = 8;
    private static final int SCAN_UP = 3;
    private static final int SCAN_DOWN = 7;
    private static final int BASE_CYCLE_TIME = 10;
    private static final int ANCIENT_PILLAR_CYCLE_BONUS = 1;
    private static final int ELDRITCH_PILLAR_CYCLE_BONUS = 3;
    private static final float ANCIENT_PILLAR_COST_BONUS = 0.1F;
    private static final float ANCIENT_PILLAR_STABILITY_PENALTY = 0.1F;
    private static final float ELDRITCH_PILLAR_COST_PENALTY = 0.05F;
    private static final float ELDRITCH_PILLAR_STABILITY_BONUS = 0.2F;
    private static final float ELDRITCH_PEDESTAL_COST_PENALTY = 0.0025F;
    private static final float ANCIENT_PEDESTAL_COST_BONUS = 0.01F;
    private static final int MATRIX_SPEED_CYCLE_BONUS = 1;
    private static final float MATRIX_SPEED_COST_PENALTY = 0.01F;
    private static final int MATRIX_COST_CYCLE_PENALTY = 1;
    private static final float MATRIX_COST_COST_BONUS = 0.02F;

    public static MatrixEnvironment survey(Level level, BlockPos matrixPos) {
        List<BlockPos> pedestals = new ArrayList<>();
        int cycleTime = BASE_CYCLE_TIME;
        float costMult = 1.0F;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -SCAN_RADIUS; xx <= SCAN_RADIUS; xx++) {
            for (int zz = -SCAN_RADIUS; zz <= SCAN_RADIUS; zz++) {
                if (xx == 0 && zz == 0) {
                    continue;
                }
                for (int yy = -SCAN_UP; yy <= SCAN_DOWN; yy++) {
                    cursor.set(matrixPos.getX() + xx, matrixPos.getY() - yy, matrixPos.getZ() + zz);
                    if (level.getBlockState(cursor).getBlock() instanceof BlockPedestal) {
                        pedestals.add(cursor.immutable());
                    }
                }
            }
        }
        float stabilityReplenish = InfusionStabilitySurvey.survey(level, matrixPos).stabilityReplenish();
        Block corner1 = pillarAt(level, matrixPos, -1, -1);
        Block corner2 = pillarAt(level, matrixPos, 1, -1);
        Block corner3 = pillarAt(level, matrixPos, 1, 1);
        Block corner4 = pillarAt(level, matrixPos, -1, 1);
        if (corner1 != null && corner1 == corner2 && corner2 == corner3 && corner3 == corner4) {
            if (corner1 == TCBlocks.PILLAR_ANCIENT.get()) {
                cycleTime -= ANCIENT_PILLAR_CYCLE_BONUS;
                costMult -= ANCIENT_PILLAR_COST_BONUS;
                stabilityReplenish -= ANCIENT_PILLAR_STABILITY_PENALTY;
            } else if (corner1 == TCBlocks.PILLAR_ELDRITCH.get()) {
                cycleTime -= ELDRITCH_PILLAR_CYCLE_BONUS;
                costMult += ELDRITCH_PILLAR_COST_PENALTY;
                stabilityReplenish += ELDRITCH_PILLAR_STABILITY_BONUS;
            }
        }
        int[] xm = {-1, 1, 1, -1};
        int[] zm = {-1, -1, 1, 1};
        for (int a = 0; a < 4; a++) {
            Block corner = level.getBlockState(matrixPos.offset(xm[a], -3, zm[a])).getBlock();
            if (corner == TCBlocks.MATRIX_SPEED.get()) {
                cycleTime -= MATRIX_SPEED_CYCLE_BONUS;
                costMult += MATRIX_SPEED_COST_PENALTY;
            } else if (corner == TCBlocks.MATRIX_COST.get()) {
                cycleTime += MATRIX_COST_CYCLE_PENALTY;
                costMult -= MATRIX_COST_COST_BONUS;
            }
        }
        for (BlockPos pedestal : pedestals) {
            Block block = level.getBlockState(pedestal).getBlock();
            if (block == TCBlocks.PEDESTAL_ELDRITCH.get()) {
                costMult += ELDRITCH_PEDESTAL_COST_PENALTY;
            } else if (block == TCBlocks.PEDESTAL_ANCIENT.get()) {
                costMult -= ANCIENT_PEDESTAL_COST_BONUS;
            }
        }
        return new MatrixEnvironment(List.copyOf(pedestals), cycleTime, costMult, stabilityReplenish);
    }

    private static Block pillarAt(Level level, BlockPos matrixPos, int dx, int dz) {
        BlockState state = level.getBlockState(matrixPos.offset(dx, -2, dz));
        return state.getBlock() instanceof BlockPillar pillar ? pillar : null;
    }

    public static boolean validLocation(Level level, BlockPos matrixPos) {
        if (!(level.getBlockState(matrixPos.offset(0, -2, 0)).getBlock() instanceof BlockPedestal)) {
            return false;
        }
        return level.getBlockState(matrixPos.offset(1, -2, 1)).getBlock() instanceof BlockPillar && level.getBlockState(matrixPos.offset(-1, -2, 1)).getBlock() instanceof BlockPillar
                && level.getBlockState(matrixPos.offset(1, -2, -1)).getBlock() instanceof BlockPillar && level.getBlockState(matrixPos.offset(-1, -2, -1)).getBlock() instanceof BlockPillar;
    }
}
