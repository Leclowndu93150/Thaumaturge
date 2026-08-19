package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class MultiblockMatcher {
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

    private MultiblockMatcher() {}

    public static @Nullable DustTriggerPlacement find(Level level, BlockPos clicked, Blueprint blueprint) {
        int ys = blueprint.ySize();
        int horizontal = Math.max(blueprint.xSize(), blueprint.zSize());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int yy = -ys; yy <= 0; yy++) {
            for (int xx = -horizontal; xx <= 0; xx++) {
                for (int zz = -horizontal; zz <= 0; zz++) {
                    cursor.set(clicked.getX() + xx, clicked.getY() + yy, clicked.getZ() + zz);
                    Direction facing = fitMultiblock(level, cursor, blueprint);
                    if (facing != null) {
                        return new DustTriggerPlacement(xx, yy, zz, facing);
                    }
                }
            }
        }
        return null;
    }

    private static @Nullable Direction fitMultiblock(Level level, BlockPos origin, Blueprint blueprint) {
        int ys = blueprint.ySize();
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        outer : for (Direction face : HORIZONTALS) {
            int rotations = 3 - horizontalIndex(face);
            for (int y = 0; y < ys; y++) {
                BlueprintMatrix matrix = new BlueprintMatrix(blueprint, y);
                matrix.rotate90DegRight(rotations);
                for (int x = 0; x < matrix.rows(); x++) {
                    for (int z = 0; z < matrix.cols(); z++) {
                        BlueprintPart part = matrix.get(x, z);
                        if (part == null) {
                            continue;
                        }
                        probe.set(origin.getX() + x, origin.getY() + (-y + (ys - 1)), origin.getZ() + z);
                        BlockState state = level.getBlockState(probe);
                        if (!part.source().matches(state)) {
                            continue outer;
                        }
                    }
                }
            }
            return face;
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
