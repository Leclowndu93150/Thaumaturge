package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomVoxels;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.jspecify.annotations.Nullable;

final class RoomRounding {
    private static final int CLEARANCE = 3;

    private RoomRounding() {}

    static void apply(RoomCanvas canvas) {
        List<Placement> placements = new ArrayList<>();
        for (int x = 0; x < canvas.sizeX(); x++) {
            for (int y = 0; y < canvas.sizeY(); y++) {
                for (int z = 0; z < canvas.sizeZ(); z++) {
                    Placement placement = placement(canvas, x, y, z);
                    if (placement != null) {
                        placements.add(placement);
                    }
                }
            }
        }
        for (Placement placement : placements) {
            canvas.set(
                    placement.pos().getX(),
                    placement.pos().getY(),
                    placement.pos().getZ(),
                    LabyrinthBlocks.stoneStairs(placement.facing(), placement.half(), StairsShape.STRAIGHT));
        }
        for (Placement placement : placements) {
            BlockPos pos = placement.pos();
            BlockState state = canvas.get(pos.getX(), pos.getY(), pos.getZ());
            canvas.set(pos.getX(), pos.getY(), pos.getZ(), state.setValue(StairBlock.SHAPE, shape(canvas, pos, state)));
        }
    }

    private static @Nullable Placement placement(RoomCanvas canvas, int x, int y, int z) {
        if (canvas.kind(x, y, z) != RoomVoxels.Kind.AIR) {
            return null;
        }
        boolean floor = host(canvas, x, y - 1, z) && clear(canvas, x, y, z, Direction.UP);
        boolean ceiling = host(canvas, x, y + 1, z) && clear(canvas, x, y, z, Direction.DOWN);
        if (floor == ceiling) {
            return null;
        }
        for (Direction wall : Direction.Plane.HORIZONTAL) {
            if (host(canvas, x + wall.getStepX(), y, z + wall.getStepZ())
                    && canvas.kind(x - wall.getStepX(), y, z - wall.getStepZ()) == RoomVoxels.Kind.AIR) {
                return new Placement(new BlockPos(x, y, z), wall, floor ? Half.BOTTOM : Half.TOP);
            }
        }
        return null;
    }

    private static boolean host(RoomCanvas canvas, int x, int y, int z) {
        BlockState state = canvas.get(x, y, z);
        return state != null && LabyrinthBlocks.roundingHost(state);
    }

    private static boolean clear(RoomCanvas canvas, int x, int y, int z, Direction away) {
        for (int step = 0; step < CLEARANCE; step++) {
            if (canvas.kind(x, y + away.getStepY() * step, z) != RoomVoxels.Kind.AIR) {
                return false;
            }
        }
        return true;
    }

    private static StairsShape shape(RoomCanvas canvas, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(StairBlock.FACING);
        BlockState behind = stairs(canvas, pos.relative(facing));
        if (behind != null && behind.getValue(StairBlock.HALF) == state.getValue(StairBlock.HALF)) {
            Direction behindFacing = behind.getValue(StairBlock.FACING);
            if (behindFacing.getAxis() != facing.getAxis()
                    && takesShape(canvas, pos, state, behindFacing.getOpposite())) {
                return behindFacing == facing.getCounterClockWise() ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
            }
        }
        BlockState front = stairs(canvas, pos.relative(facing.getOpposite()));
        if (front != null && front.getValue(StairBlock.HALF) == state.getValue(StairBlock.HALF)) {
            Direction frontFacing = front.getValue(StairBlock.FACING);
            if (frontFacing.getAxis() != facing.getAxis() && takesShape(canvas, pos, state, frontFacing)) {
                return frontFacing == facing.getCounterClockWise() ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
            }
        }
        return StairsShape.STRAIGHT;
    }

    private static boolean takesShape(RoomCanvas canvas, BlockPos pos, BlockState state, Direction side) {
        BlockState neighbour = stairs(canvas, pos.relative(side));
        return neighbour == null
                || neighbour.getValue(StairBlock.FACING) != state.getValue(StairBlock.FACING)
                || neighbour.getValue(StairBlock.HALF) != state.getValue(StairBlock.HALF);
    }

    private static @Nullable BlockState stairs(RoomCanvas canvas, BlockPos pos) {
        BlockState state = canvas.get(pos.getX(), pos.getY(), pos.getZ());
        return state != null && state.getBlock() instanceof StairBlock ? state : null;
    }

    private record Placement(BlockPos pos, Direction facing, Half half) {}
}
