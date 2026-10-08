package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GolemPressShapes {
    private static final VoxelShape BODY = Shapes.or(
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(1, 2, 1, 15, 12, 15),
            Block.box(0, 2, 0, 2, 12, 2),
            Block.box(14, 2, 0, 16, 12, 2),
            Block.box(0, 2, 14, 2, 12, 16),
            Block.box(14, 2, 14, 16, 12, 16),
            Block.box(0, 12, 0, 16, 16, 16),
            Block.box(7, 15, 16, 9, 16, 21),
            Block.box(7, 13, 20, 9, 15, 21));
    private static final VoxelShape FRAME = Shapes.or(
            Block.box(0, 16, 0, 2, 30, 2),
            Block.box(14, 16, 0, 16, 30, 2),
            Block.box(0, 16, 14, 2, 30, 16),
            Block.box(14, 16, 14, 16, 30, 16),
            Block.box(0, 30, 0, 16, 32, 2),
            Block.box(0, 30, 14, 16, 32, 16));
    private static final VoxelShape PRESS = Shapes.or(
            Block.box(2, 27, 2, 14, 30, 14),
            Block.box(3, 30, 3, 5, 32, 5),
            Block.box(11, 30, 3, 13, 32, 5),
            Block.box(3, 30, 11, 5, 32, 13),
            Block.box(11, 30, 11, 13, 32, 13),
            Block.box(-1, 27, -1, 3, 29, 3),
            Block.box(13, 27, -1, 17, 29, 3),
            Block.box(-1, 27, 13, 3, 29, 17),
            Block.box(13, 27, 13, 17, 29, 17));
    private static final VoxelShape TABLE = Shapes.or(
            Block.box(16, 12, 0, 32, 16, 16),
            Block.box(17, 0, 1, 21, 12, 5),
            Block.box(27, 0, 1, 31, 12, 5),
            Block.box(17, 0, 11, 21, 12, 15),
            Block.box(27, 0, 11, 31, 12, 15),
            Block.box(18, 16, 2, 30, 17, 14));
    private static final VoxelShape CAULDRON = Shapes.or(
            Block.box(1, 0, 17, 15, 10, 31),
            Block.box(1, 10, 17, 3, 14, 31),
            Block.box(13, 10, 17, 15, 14, 31),
            Block.box(3, 10, 17, 13, 14, 19),
            Block.box(3, 10, 29, 13, 14, 31));
    private static final VoxelShape ANVIL = Shapes.or(
            Block.box(17, 0, 17, 31, 4, 31), Block.box(21, 4, 20, 27, 10, 28), Block.box(16, 10, 17, 32, 16, 31));
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    private GolemPressShapes() {}

    public static VoxelShape at(Direction facing, BlockPos offset) {
        return Shapes.join(
                SHAPES.get(facing).move(-offset.getX(), -offset.getY(), -offset.getZ()), Shapes.block(), BooleanOp.AND);
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        VoxelShape north = Shapes.or(BODY, FRAME, PRESS, TABLE, CAULDRON, ANVIL);
        Map<Direction, VoxelShape> result = new EnumMap<>(Direction.class);
        result.put(Direction.NORTH, north);
        result.put(Direction.EAST, DeviceShapes.rotate(north, 0, 1).optimize());
        result.put(Direction.SOUTH, DeviceShapes.rotate(north, 0, 2).optimize());
        result.put(Direction.WEST, DeviceShapes.rotate(north, 0, 3).optimize());
        return result;
    }
}
