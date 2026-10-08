package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

public final class StairVoxels {
    public static final int FULL = 0xFF;

    private static final int FACINGS = 4;
    private static final Half[] HALVES = Half.values();
    private static final StairsShape[] SHAPES = StairsShape.values();
    private static final int[] MASKS = buildMasks();

    private StairVoxels() {}

    public static int bit(int x, int y, int z) {
        return 1 << (x | z << 1 | y << 2);
    }

    public static boolean isStairs(BlockState state) {
        return state.getBlock() instanceof StairBlock;
    }

    public static int mask(BlockState state) {
        if (!isStairs(state)) {
            return mask(Direction.NORTH, Half.BOTTOM, StairsShape.STRAIGHT);
        }
        return mask(
                state.getValue(StairBlock.FACING), state.getValue(StairBlock.HALF), state.getValue(StairBlock.SHAPE));
    }

    public static int mask(Direction facing, Half half, StairsShape shape) {
        return MASKS[index(facing, half, shape)];
    }

    private static int index(Direction facing, Half half, StairsShape shape) {
        return (facing.get2DDataValue() * HALVES.length + half.ordinal()) * SHAPES.length + shape.ordinal();
    }

    private static int[] buildMasks() {
        int[] masks = new int[FACINGS * HALVES.length * SHAPES.length];
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Half half : HALVES) {
                for (StairsShape shape : SHAPES) {
                    masks[index(facing, half, shape)] = build(facing, half, shape);
                }
            }
        }
        return masks;
    }

    private static int build(Direction facing, Half half, StairsShape shape) {
        int low = half == Half.BOTTOM ? 0 : 1;
        int high = 1 - low;
        int mask = 0;
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                mask |= bit(x, low, z);
                if (raised(facing, shape, x, z)) {
                    mask |= bit(x, high, z);
                }
            }
        }
        return mask;
    }

    private static boolean raised(Direction facing, StairsShape shape, int x, int z) {
        boolean back = toward(x, z, facing);
        return switch (shape) {
            case STRAIGHT -> back;
            case OUTER_LEFT -> back && toward(x, z, facing.getCounterClockWise());
            case OUTER_RIGHT -> back && toward(x, z, facing.getClockWise());
            case INNER_LEFT -> back || toward(x, z, facing.getCounterClockWise());
            case INNER_RIGHT -> back || toward(x, z, facing.getClockWise());
        };
    }

    private static boolean toward(int x, int z, Direction direction) {
        return (2 * x - 1) * direction.getStepX() + (2 * z - 1) * direction.getStepZ() > 0;
    }
}
