package com.leclowndu93150.thaumaturge.content.eldritch.lock;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

public final class LockDoorway {
    private static final int DOOR_SPAN = 6;
    private static final int LOCK_REACH = 4;
    private static final float HALF_BLOCK = 0.5F;

    private LockDoorway() {}

    public static Direction across(Direction facing) {
        return facing.getAxis().isHorizontal() ? facing.getClockWise() : Direction.EAST;
    }

    public static float centreShift(BlockGetter level, BlockPos lock, Direction facing) {
        if (!facing.getAxis().isHorizontal()) {
            return 0.0F;
        }
        Direction across = across(facing);
        return (doorRun(level, lock, across) - doorRun(level, lock, across.getOpposite())) * HALF_BLOCK;
    }

    public static Optional<BlockPos> findLock(BlockGetter level, BlockPos door) {
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                door.offset(-LOCK_REACH, -LOCK_REACH, -LOCK_REACH), door.offset(LOCK_REACH, LOCK_REACH, LOCK_REACH))) {
            double distance = pos.distSqr(door);
            if (distance < best && level.getBlockState(pos).is(TTBlocks.ELDRITCH_LOCK.get())) {
                nearest = pos.immutable();
                best = distance;
            }
        }
        return Optional.ofNullable(nearest);
    }

    private static int doorRun(BlockGetter level, BlockPos lock, Direction side) {
        BlockPos.MutableBlockPos cursor = lock.mutable();
        int run = 0;
        while (run < DOOR_SPAN && level.getBlockState(cursor.move(side)).is(TTBlockTags.LABYRINTH_BARRIER)) {
            run++;
        }
        return run;
    }
}
