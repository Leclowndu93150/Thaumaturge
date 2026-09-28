package com.leclowndu93150.thaumaturge.content.golem.press;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Shapes in the same coordinates and rotation as the golem builder mesh. */
public final class GolemPressShapes {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    private GolemPressShapes() {}

    public static VoxelShape at(Direction facing, BlockPos offset) {
        // Each multiblock cell owns only its portion of the model. This avoids
        // selecting or colliding with geometry through another block's cell.
        return Shapes.join(
                SHAPES.get(facing).move(-offset.getX(), -offset.getY(), -offset.getZ()), Shapes.block(), BooleanOp.AND);
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        VoxelShape north = Shapes.or(
                Block.box(0, 0, 0, 16, 12, 16),
                Block.box(4, 12, 4, 12, 16, 12),
                Block.box(2, 16, 2, 4, 32, 4),
                Block.box(12, 16, 2, 14, 32, 4),
                Block.box(2, 16, 12, 4, 32, 14),
                Block.box(12, 16, 12, 14, 32, 14),
                Block.box(0, 22, 0, 16, 32, 16),
                Block.box(16, 12, 0, 32, 16, 16),
                Block.box(28, 0, 0, 32, 4, 16),
                Block.box(28, 4, 4, 32, 12, 12),
                Block.box(1, 0, 17, 15, 2, 31),
                Block.box(1, 2, 17, 3, 14, 31),
                Block.box(13, 2, 17, 15, 14, 31),
                Block.box(3, 2, 17, 13, 14, 19),
                Block.box(3, 2, 29, 13, 14, 31),
                Block.box(17, 0, 17, 31, 4, 31),
                Block.box(21, 4, 20, 27, 10, 28),
                Block.box(16, 10, 17, 32, 16, 31));
        Map<Direction, VoxelShape> result = new EnumMap<>(Direction.class);
        result.put(Direction.NORTH, north);
        VoxelShape rotated = north;
        // Renderer rotations: NORTH=0, WEST=90, SOUTH=180, EAST=270.
        for (Direction facing : new Direction[] {Direction.WEST, Direction.SOUTH, Direction.EAST}) {
            VoxelShape[] next = {Shapes.empty()};
            rotated.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    next[0] = Shapes.or(next[0], Shapes.box(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX)));
            rotated = next[0].optimize();
            result.put(facing, rotated);
        }
        return result;
    }
}
