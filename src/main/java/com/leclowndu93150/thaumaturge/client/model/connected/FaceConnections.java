package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class FaceConnections {
    public static final int TOP_LEFT = 1;
    public static final int TOP = 1 << 1;
    public static final int TOP_RIGHT = 1 << 2;
    public static final int LEFT = 1 << 3;
    public static final int RIGHT = 1 << 4;
    public static final int BOTTOM_LEFT = 1 << 5;
    public static final int BOTTOM = 1 << 6;
    public static final int BOTTOM_RIGHT = 1 << 7;
    public static final int MASKS = 1 << 8;

    private static final int NEIGHBOURS = 8;
    private static final int[] COLUMN = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int[] ROW = {-1, -1, -1, 0, 0, 1, 1, 1};

    private FaceConnections() {}

    public static boolean has(int mask, int neighbour) {
        return (mask & neighbour) != 0;
    }

    public static int mask(
            BlockGetter level,
            BlockPos pos,
            BlockState state,
            Direction face,
            @Nullable TagKey<Block> group,
            BlockPos.MutableBlockPos cursor) {
        if (!joins(state, state, group)) {
            return 0;
        }
        Direction up = textureUp(face);
        Direction right = textureRight(face);
        int mask = 0;
        for (int bit = 0; bit < NEIGHBOURS; bit++) {
            cursor.set(pos).move(up, -ROW[bit]).move(right, COLUMN[bit]);
            if (connects(level, state, group, face, cursor)) {
                mask |= 1 << bit;
            }
        }
        return mask;
    }

    private static boolean connects(
            BlockGetter level,
            BlockState state,
            @Nullable TagKey<Block> group,
            Direction face,
            BlockPos.MutableBlockPos cursor) {
        if (!joins(state, level.getBlockState(cursor), group)) {
            return false;
        }
        cursor.move(face);
        return !joins(state, level.getBlockState(cursor), group);
    }

    private static boolean joins(BlockState state, BlockState other, @Nullable TagKey<Block> group) {
        if (group != null) {
            return other.is(group);
        }
        return !state.isAir() && other.is(state.getBlock());
    }

    static Direction textureUp(Direction face) {
        return switch (face) {
            case UP -> Direction.NORTH;
            case DOWN -> Direction.SOUTH;
            default -> Direction.UP;
        };
    }

    static Direction textureRight(Direction face) {
        return switch (face) {
            case UP, DOWN, SOUTH -> Direction.EAST;
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case EAST -> Direction.NORTH;
        };
    }
}
