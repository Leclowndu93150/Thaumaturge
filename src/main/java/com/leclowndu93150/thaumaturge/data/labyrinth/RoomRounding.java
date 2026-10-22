package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomVoxels;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.jspecify.annotations.Nullable;

final class RoomRounding {
    private static final int CLEARANCE = 3;
    private static final int POCKET_PASSES = 4;
    private static final int ROUNDS = 3;
    private static final Direction[] AXES = {Direction.EAST, Direction.SOUTH};
    private static final Direction[] WALL_ORDER = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private RoomRounding() {}

    static void apply(RoomCanvas canvas, BlockState groove) {
        Map<BlockPos, Placement> placements = new LinkedHashMap<>();
        for (int round = 0; round < ROUNDS; round++) {
            placements.values().removeIf(placement -> stairs(canvas, placement.pos()) == null);
            round(canvas, placements, round > 0);
            fillCeilingPockets(canvas, groove);
        }
    }

    private static void round(RoomCanvas canvas, Map<BlockPos, Placement> placements, boolean extendOnly) {
        Map<BlockPos, Placement> fresh = new LinkedHashMap<>();
        for (int x = 0; x < canvas.sizeX(); x++) {
            for (int y = 0; y < canvas.sizeY(); y++) {
                for (int z = 0; z < canvas.sizeZ(); z++) {
                    Placement placement = placement(canvas, x, y, z);
                    if (placement != null) {
                        fresh.put(placement.pos(), placement);
                    }
                }
            }
        }
        placements.putAll(extendOnly ? connected(fresh, placements) : fresh);
        extendThroughSlots(canvas, placements);
        Map<BlockPos, Placement> corners = new LinkedHashMap<>();
        for (int x = 0; x < canvas.sizeX(); x++) {
            for (int y = 0; y < canvas.sizeY(); y++) {
                for (int z = 0; z < canvas.sizeZ(); z++) {
                    Placement corner = corner(canvas, placements, x, y, z);
                    if (corner != null) {
                        corners.put(corner.pos(), corner);
                    }
                }
            }
        }
        placements.putAll(corners);
        for (Placement placement : placements.values()) {
            canvas.set(
                    placement.pos().getX(),
                    placement.pos().getY(),
                    placement.pos().getZ(),
                    LabyrinthBlocks.stoneStairs(placement.facing(), placement.half(), StairsShape.STRAIGHT));
        }
        for (Placement placement : placements.values()) {
            BlockPos pos = placement.pos();
            BlockState state = canvas.get(pos.getX(), pos.getY(), pos.getZ());
            canvas.set(pos.getX(), pos.getY(), pos.getZ(), state.setValue(StairBlock.SHAPE, shape(canvas, pos, state)));
        }
    }

    private static Map<BlockPos, Placement> connected(
            Map<BlockPos, Placement> fresh, Map<BlockPos, Placement> placements) {
        Map<BlockPos, Placement> accepted = new LinkedHashMap<>();
        boolean grew = true;
        while (grew) {
            grew = false;
            for (Placement placement : fresh.values()) {
                if (!accepted.containsKey(placement.pos())
                        && (continues(placements, placement) || continues(accepted, placement))) {
                    accepted.put(placement.pos(), placement);
                    grew = true;
                }
            }
        }
        return accepted;
    }

    private static boolean continues(Map<BlockPos, Placement> runs, Placement placement) {
        return sameRun(runs.get(placement.pos().relative(placement.facing().getClockWise())), placement)
                || sameRun(runs.get(placement.pos().relative(placement.facing().getCounterClockWise())), placement);
    }

    private static boolean sameRun(@Nullable Placement neighbour, Placement placement) {
        return neighbour != null && neighbour.facing() == placement.facing() && neighbour.half() == placement.half();
    }

    private static @Nullable Half half(RoomCanvas canvas, int x, int y, int z) {
        if (canvas.kind(x, y, z) != RoomVoxels.Kind.AIR) {
            return null;
        }
        BlockState below = canvas.get(x, y - 1, z);
        boolean floor = below != null && LabyrinthBlocks.roundingFloor(below) && clear(canvas, x, y, z, Direction.UP);
        boolean ceiling = host(canvas, x, y + 1, z) && clear(canvas, x, y, z, Direction.DOWN);
        if (floor == ceiling) {
            return null;
        }
        return floor ? Half.BOTTOM : Half.TOP;
    }

    private static @Nullable Half cornerHalf(RoomCanvas canvas, Map<BlockPos, Placement> placements, BlockPos pos) {
        Half half = half(canvas, pos.getX(), pos.getY(), pos.getZ());
        if (half != null || canvas.kind(pos.getX(), pos.getY(), pos.getZ()) != RoomVoxels.Kind.AIR) {
            return half;
        }
        boolean roofed =
                occupied(canvas, pos.getX(), pos.getY() + 1, pos.getZ()) || placements.containsKey(pos.above());
        return roofed && clear(canvas, pos.getX(), pos.getY(), pos.getZ(), Direction.DOWN) ? Half.TOP : null;
    }

    private static @Nullable Placement placement(RoomCanvas canvas, int x, int y, int z) {
        Half half = half(canvas, x, y, z);
        if (half == null) {
            return null;
        }
        for (Direction wall : WALL_ORDER) {
            if (host(canvas, x + wall.getStepX(), y, z + wall.getStepZ())
                    && clear(canvas.kind(x - wall.getStepX(), y, z - wall.getStepZ()))) {
                return new Placement(new BlockPos(x, y, z), wall, half);
            }
        }
        return null;
    }

    private static @Nullable Placement corner(
            RoomCanvas canvas, Map<BlockPos, Placement> placements, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Half half = cornerHalf(canvas, placements, pos);
        if (half == null || placements.containsKey(pos)) {
            return null;
        }
        for (Direction first : Direction.Plane.HORIZONTAL) {
            if (wraps(canvas, placements, pos, half, first, first.getClockWise())
                    || wraps(canvas, placements, pos, half, first, first.getCounterClockWise())) {
                return new Placement(pos, first, half);
            }
        }
        return null;
    }

    private static boolean wraps(
            RoomCanvas canvas,
            Map<BlockPos, Placement> placements,
            BlockPos pos,
            Half half,
            Direction first,
            Direction second) {
        Placement along = placements.get(pos.relative(first));
        Placement across = placements.get(pos.relative(second));
        return along != null
                && across != null
                && along.half() == half
                && across.half() == half
                && along.facing() == second
                && across.facing() == first
                && host(
                        canvas,
                        pos.getX() + first.getStepX() + second.getStepX(),
                        pos.getY(),
                        pos.getZ() + first.getStepZ() + second.getStepZ());
    }

    private static void extendThroughSlots(RoomCanvas canvas, Map<BlockPos, Placement> placements) {
        for (int pass = 0; pass < POCKET_PASSES; pass++) {
            List<Placement> added = new ArrayList<>();
            for (int x = 0; x < canvas.sizeX(); x++) {
                for (int y = 0; y < canvas.sizeY(); y++) {
                    for (int z = 0; z < canvas.sizeZ(); z++) {
                        Placement slot = slotPlacement(canvas, placements, x, y, z);
                        if (slot != null) {
                            added.add(slot);
                        }
                    }
                }
            }
            if (added.isEmpty()) {
                return;
            }
            for (Placement placement : added) {
                placements.put(placement.pos(), placement);
            }
        }
    }

    private static @Nullable Placement slotPlacement(
            RoomCanvas canvas, Map<BlockPos, Placement> placements, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (placements.containsKey(pos) || half(canvas, x, y, z) != Half.BOTTOM) {
            return null;
        }
        for (Direction wall : WALL_ORDER) {
            if (host(canvas, x + wall.getStepX(), y, z + wall.getStepZ())
                    && (runsAlong(placements.get(pos.relative(wall.getClockWise())), wall)
                            || runsAlong(placements.get(pos.relative(wall.getCounterClockWise())), wall))) {
                return new Placement(pos, wall, Half.BOTTOM);
            }
        }
        return null;
    }

    private static boolean runsAlong(@Nullable Placement neighbour, Direction wall) {
        return neighbour != null && neighbour.facing() == wall && neighbour.half() == Half.BOTTOM;
    }

    private static void fillCeilingPockets(RoomCanvas canvas, BlockState fill) {
        for (int pass = 0; pass < POCKET_PASSES; pass++) {
            List<BlockPos> pockets = new ArrayList<>();
            for (int x = 0; x < canvas.sizeX(); x++) {
                for (int y = 0; y < canvas.sizeY(); y++) {
                    for (int z = 0; z < canvas.sizeZ(); z++) {
                        if (ceilingPocket(canvas, x, y, z) || blockedCornice(canvas, x, y, z)) {
                            pockets.add(new BlockPos(x, y, z));
                        }
                    }
                }
            }
            if (pockets.isEmpty()) {
                return;
            }
            for (BlockPos pocket : pockets) {
                canvas.set(pocket.getX(), pocket.getY(), pocket.getZ(), fill);
            }
        }
    }

    private static boolean ceilingPocket(RoomCanvas canvas, int x, int y, int z) {
        if (canvas.kind(x, y, z) != RoomVoxels.Kind.AIR
                || !occupied(canvas, x, y + 1, z)
                || canvas.kind(x, y - 1, z) != RoomVoxels.Kind.AIR) {
            return false;
        }
        for (Direction side : AXES) {
            if (slot(canvas, x, y, z, side) && !slot(canvas, x, y - 1, z, side) && !arch(canvas, x, y, z, side)) {
                return true;
            }
        }
        return false;
    }

    private static boolean blockedCornice(RoomCanvas canvas, int x, int y, int z) {
        BlockState state = stairs(canvas, new BlockPos(x, y, z));
        if (state == null
                || state.getValue(StairBlock.HALF) != Half.TOP
                || state.getValue(StairBlock.SHAPE) != StairsShape.STRAIGHT) {
            return false;
        }
        Direction step = state.getValue(StairBlock.FACING).getOpposite();
        return canvas.kind(x + step.getStepX(), y, z + step.getStepZ()) == RoomVoxels.Kind.SOLID;
    }

    private static boolean arch(RoomCanvas canvas, int x, int y, int z, Direction side) {
        BlockState one = stairs(canvas, new BlockPos(x + side.getStepX(), y, z + side.getStepZ()));
        BlockState other = stairs(canvas, new BlockPos(x - side.getStepX(), y, z - side.getStepZ()));
        return one != null && other != null && !stepTouches(one, side.getOpposite()) && !stepTouches(other, side);
    }

    private static boolean stepTouches(BlockState stairs, Direction face) {
        Direction facing = stairs.getValue(StairBlock.FACING);
        return switch (stairs.getValue(StairBlock.SHAPE)) {
            case STRAIGHT -> face != facing.getOpposite();
            case OUTER_LEFT -> face != facing.getOpposite() && face != facing.getClockWise();
            case OUTER_RIGHT -> face != facing.getOpposite() && face != facing.getCounterClockWise();
            case INNER_LEFT, INNER_RIGHT -> true;
        };
    }

    private static boolean slot(RoomCanvas canvas, int x, int y, int z, Direction side) {
        return occupied(canvas, x + side.getStepX(), y, z + side.getStepZ())
                && occupied(canvas, x - side.getStepX(), y, z - side.getStepZ());
    }

    private static boolean occupied(RoomCanvas canvas, int x, int y, int z) {
        RoomVoxels.Kind kind = canvas.kind(x, y, z);
        return kind == RoomVoxels.Kind.SOLID || kind == RoomVoxels.Kind.PASSABLE;
    }

    private static boolean host(RoomCanvas canvas, int x, int y, int z) {
        BlockState state = canvas.get(x, y, z);
        return state != null && LabyrinthBlocks.roundingHost(state);
    }

    private static boolean clear(RoomCanvas canvas, int x, int y, int z, Direction away) {
        for (int step = 0; step < CLEARANCE; step++) {
            if (!clear(canvas.kind(x, y + away.getStepY() * step, z))) {
                return false;
            }
        }
        return true;
    }

    private static boolean clear(RoomVoxels.Kind kind) {
        return kind == RoomVoxels.Kind.AIR || kind == RoomVoxels.Kind.MARKER;
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
