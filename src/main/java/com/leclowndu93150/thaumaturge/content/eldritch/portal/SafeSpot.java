package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class SafeSpot {
    private SafeSpot() {}

    public static boolean standable(ServerLevel level, BlockPos feet) {
        BlockPos floorPos = feet.below();
        BlockState floor = level.getBlockState(floorPos);
        return floor.isFaceSturdy(level, floorPos, Direction.UP)
                && !floor.is(TTBlockTags.UNSAFE_LANDING)
                && floor.getFluidState().isEmpty()
                && clear(level, feet)
                && clear(level, feet.above());
    }

    public static boolean clear(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty()
                && state.getFluidState().isEmpty()
                && !state.is(TTBlockTags.UNSAFE_LANDING);
    }

    public static Optional<BlockPos> surface(ServerLevel level, int x, int z) {
        BlockPos column = new BlockPos(x, level.getMinBuildHeight(), z);
        if (!level.hasChunkAt(column)) {
            return Optional.empty();
        }
        BlockPos feet = column.atY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z));
        return standable(level, feet) ? Optional.of(feet) : Optional.empty();
    }

    public static Optional<BlockPos> near(ServerLevel level, BlockPos center, int horizontal, int vertical) {
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-horizontal, -vertical, -horizontal), center.offset(horizontal, vertical, horizontal))) {
            candidates.add(pos.immutable());
        }
        candidates.sort(Comparator.comparingDouble((BlockPos pos) -> pos.distSqr(center))
                .thenComparingLong(BlockPos::asLong));
        for (BlockPos pos : candidates) {
            if (level.hasChunkAt(pos) && standable(level, pos)) {
                return Optional.of(pos);
            }
        }
        return Optional.empty();
    }
}
