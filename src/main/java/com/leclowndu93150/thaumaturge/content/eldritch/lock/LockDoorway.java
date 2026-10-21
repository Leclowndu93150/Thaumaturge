package com.leclowndu93150.thaumaturge.content.eldritch.lock;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public final class LockDoorway {
    private static final int LOCK_REACH = 4;

    private LockDoorway() {}

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
}
