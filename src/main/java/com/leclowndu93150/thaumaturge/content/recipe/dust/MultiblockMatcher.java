package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class MultiblockMatcher {
    private static final Direction[] HORIZONTALS =
            new Direction[] {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

    private MultiblockMatcher() {}

    public static @Nullable DustTriggerPlacement find(Level level, BlockPos clicked, Blueprint blueprint) {
        var clickedState = level.getBlockState(clicked);
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (Direction facing : HORIZONTALS) {
            List<BlueprintCell> cells = new RotatedBlueprint(blueprint, rotationsFor(facing)).cells();
            // The clicked block must be one of the blueprint's occupied cells.
            // Search only those candidate origins, not an entire cubic volume.
            for (BlueprintCell anchor : cells) {
                if (!anchor.part().source().matches(clickedState)) continue;
                BlockPos origin = clicked.subtract(anchor.offset());
                if (fits(level, origin, cells, probe)) {
                    return new DustTriggerPlacement(
                            origin.getX() - clicked.getX(),
                            origin.getY() - clicked.getY(),
                            origin.getZ() - clicked.getZ(),
                            facing);
                }
            }
        }
        return null;
    }

    private static boolean fits(
            Level level, BlockPos origin, List<BlueprintCell> cells, BlockPos.MutableBlockPos probe) {
        for (BlueprintCell cell : cells) {
            probe.setWithOffset(origin, cell.offset());
            if (!cell.part().source().matches(level.getBlockState(probe))) return false;
        }
        return true;
    }

    public static int horizontalIndex(Direction d) {
        return switch (d) {
            case SOUTH -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> 0;
        };
    }

    public static int rotationsFor(Direction face) {
        return 3 - horizontalIndex(face);
    }
}
