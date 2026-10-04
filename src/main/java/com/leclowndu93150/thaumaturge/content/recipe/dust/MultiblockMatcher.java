package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class MultiblockMatcher {
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

    private MultiblockMatcher() {}

    public static @Nullable DustTriggerPlacement find(Level level, BlockPos clicked, Blueprint blueprint) {
        int ys = blueprint.ySize();
        int horizontal = Math.max(blueprint.xSize(), blueprint.zSize());
        Map<Direction, List<BlueprintCell>> orientations = new EnumMap<>(Direction.class);
        for (Direction face : HORIZONTALS) {
            orientations.put(face, new RotatedBlueprint(blueprint, rotationsFor(face)).cells());
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int yy = -ys; yy <= 0; yy++) {
            for (int xx = -horizontal; xx <= 0; xx++) {
                for (int zz = -horizontal; zz <= 0; zz++) {
                    cursor.set(clicked.getX() + xx, clicked.getY() + yy, clicked.getZ() + zz);
                    Direction facing = fitMultiblock(level, cursor, orientations);
                    if (facing != null) {
                        return new DustTriggerPlacement(xx, yy, zz, facing);
                    }
                }
            }
        }
        return null;
    }

    private static @Nullable Direction fitMultiblock(Level level, BlockPos origin, Map<Direction, List<BlueprintCell>> orientations) {
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (Direction face : HORIZONTALS) {
            if (orientations.get(face).stream().allMatch(cell -> cell.part().source().matches(level.getBlockState(probe.setWithOffset(origin, cell.offset()))))) {
                return face;
            }
        }
        return null;
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
