package com.leclowndu93150.thaumaturge.content.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BellowsHelper {
    private BellowsHelper() {}

    public static int countBellows(Level level, BlockPos pos, Direction[] directions) {
        return countBellows(level, pos, directions, 1);
    }

    public static int countBellows(Level level, BlockPos pos, Direction[] directions, int offset) {
        if (level == null)
            return 0;
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : directions) {
            setWithOffset(cursor, pos, dir, offset);
            BlockEntity tile = level.getBlockEntity(cursor);
            if (tile instanceof IBellowsPower bellows && bellows.bellowsFacing() == dir.getOpposite() && bellows.bellowsEnabled()) {
                count++;
            }
        }
        return count;
    }

    private static BlockPos.MutableBlockPos setWithOffset(BlockPos.MutableBlockPos bp, Vec3i pos, Direction direction, int offset) {
        return bp.set(pos.getX() + direction.getStepX() * offset, pos.getY() + direction.getStepY() * offset, pos.getZ() + direction.getStepZ() * offset);
    }
}
