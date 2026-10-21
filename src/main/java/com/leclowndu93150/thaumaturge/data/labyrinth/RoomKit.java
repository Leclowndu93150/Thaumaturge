package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.guardian.GuardianPosts;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.BlockMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GlyphMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GuardianPostMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LootMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomVoxels;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

final class RoomKit {
    static final double MID = MazeGeometry.CELL / 2.0;

    private static final int COMMON_WEIGHT = 6;
    private static final int UNCOMMON_WEIGHT = 3;
    private static final int RARE_WEIGHT = 1;
    private static final int WALL_VENT_RISE = 2;
    private static final int NICHE_HEIGHT = 2;
    private static final int PLINTH_RISE = 2;

    private RoomKit() {}

    static LootMarker urns(float chance) {
        return tiered(
                TTBlocks.LOOT_URN_COMMON.get(), TTBlocks.LOOT_URN_UNCOMMON.get(), TTBlocks.LOOT_URN_RARE.get(), chance);
    }

    static LootMarker crates(float chance) {
        return tiered(
                TTBlocks.LOOT_CRATE_COMMON.get(),
                TTBlocks.LOOT_CRATE_UNCOMMON.get(),
                TTBlocks.LOOT_CRATE_RARE.get(),
                chance);
    }

    private static LootMarker tiered(Block common, Block uncommon, Block rare, float chance) {
        return new LootMarker(
                SimpleWeightedRandomList.<BlockState>builder()
                        .add(common.defaultBlockState(), COMMON_WEIGHT)
                        .add(uncommon.defaultBlockState(), UNCOMMON_WEIGHT)
                        .add(rare.defaultBlockState(), RARE_WEIGHT)
                        .build(),
                chance);
    }

    static BlockMarker embedded(BlockState state, float chance, BlockState otherwise) {
        return new BlockMarker(
                SimpleWeightedRandomList.single(state), chance, true, Optional.empty(), Optional.of(otherwise));
    }

    static BlockMarker scatter(BlockState state, float chance) {
        return new BlockMarker(
                SimpleWeightedRandomList.single(state), chance, true, Optional.empty(), Optional.empty());
    }

    static void floorVent(RoomCanvas canvas, int x, int z, float chance) {
        requireOpen(canvas, x, RoomCanvas.F + 1, z);
        canvas.marker(x, RoomCanvas.F, z, vent(Direction.UP, chance, surface(canvas, x, RoomCanvas.F, z)));
    }

    static void wallVent(RoomCanvas canvas, int x, int z, float chance) {
        int y = RoomCanvas.F + WALL_VENT_RISE;
        canvas.marker(x, y, z, vent(openSide(canvas, x, y, z), chance, surface(canvas, x, y, z)));
    }

    static void floorTrap(RoomCanvas canvas, int x, int z, float chance) {
        requireOpen(canvas, x, RoomCanvas.F + 1, z);
        canvas.marker(
                x, RoomCanvas.F, z, embedded(LabyrinthBlocks.trap(), chance, surface(canvas, x, RoomCanvas.F, z)));
    }

    private static BlockMarker vent(Direction facing, float chance, BlockState otherwise) {
        return new BlockMarker(
                SimpleWeightedRandomList.single(LabyrinthBlocks.crabSpawner()),
                chance,
                true,
                Optional.of(facing),
                Optional.of(otherwise));
    }

    private static BlockState surface(RoomCanvas canvas, int x, int y, int z) {
        if (canvas.kind(x, y, z).open()) {
            throw new IllegalArgumentException("Room dressing at " + x + "," + y + "," + z + " needs a solid surface");
        }
        BlockState current = canvas.get(x, y, z);
        return current == null ? LabyrinthBlocks.stone() : current;
    }

    static void niche(RoomCanvas canvas, int x, int z, LootMarker loot) {
        openSide(canvas, x, RoomCanvas.F + 1, z);
        canvas.carve(RoomShape.box(x, RoomCanvas.F + 1, z, x, RoomCanvas.F + NICHE_HEIGHT, z));
        canvas.marker(x, RoomCanvas.F + 1, z, loot);
    }

    static void floorLoot(RoomCanvas canvas, int x, int z, LootMarker loot) {
        requireOpen(canvas, x, RoomCanvas.F + 1, z);
        canvas.marker(x, RoomCanvas.F + 1, z, loot);
    }

    static void plinth(RoomCanvas canvas, int x, int z, LootMarker loot) {
        requireOpen(canvas, x, RoomCanvas.F + PLINTH_RISE, z);
        canvas.set(x, RoomCanvas.F + 1, z, LabyrinthBlocks.pedestal());
        canvas.marker(x, RoomCanvas.F + PLINTH_RISE, z, loot);
    }

    static void post(RoomCanvas canvas, int x, int z) {
        requireOpen(canvas, x, RoomCanvas.F + 1, z);
        canvas.marker(x, RoomCanvas.F + 1, z, new GuardianPostMarker(Optional.empty(), Optional.empty()));
    }

    private static void requireOpen(RoomCanvas canvas, int x, int y, int z) {
        if (canvas.kind(x, y, z) != RoomVoxels.Kind.AIR) {
            throw new IllegalArgumentException("Room dressing at " + x + "," + y + "," + z + " needs open air");
        }
    }

    private static Direction openSide(RoomCanvas canvas, int x, int y, int z) {
        if (canvas.kind(x, y, z).open()) {
            throw new IllegalArgumentException("Room dressing at " + x + "," + y + "," + z + " is not inside a wall");
        }
        Direction found = null;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (canvas.kind(x + side.getStepX(), y, z + side.getStepZ()) == RoomVoxels.Kind.AIR) {
                if (found != null) {
                    throw new IllegalArgumentException(
                            "Room dressing at " + x + "," + y + "," + z + " faces more than one opening");
                }
                found = side;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException("Room dressing at " + x + "," + y + "," + z + " faces no opening");
        }
        return found;
    }

    static GuardianPostMarker keyWard() {
        return new GuardianPostMarker(Optional.empty(), Optional.of(GuardianPosts.KEY_ROOM_WARD));
    }

    static void glyph(RoomCanvas canvas, int x, int y, int z, Direction facing) {
        canvas.marker(x, y, z, new GlyphMarker(LabyrinthBlocks.glyph(), facing));
    }

    static void entrance(RoomCanvas canvas, int depth) {
        canvas.socket(new RoomSocket(0, 0, Direction.NORTH), depth);
    }

    static void column(RoomCanvas canvas, int x, int z, int y0, int y1) {
        column(canvas, x, z, x, z, y0, y1);
    }

    static void column(RoomCanvas canvas, int x0, int z0, int x1, int z1, int y0, int y1) {
        canvas.fill(RoomShape.box(x0, y0, z0, x1, y0, z1), LabyrinthBlocks.tile());
        canvas.fill(RoomShape.box(x0, y0 + 1, z0, x1, y1 - 1, z1), LabyrinthBlocks.column(Direction.Axis.Y));
        canvas.fill(RoomShape.box(x0, y1, z0, x1, y1, z1), LabyrinthBlocks.tile());
    }

    static void columns(RoomCanvas canvas, int[][] spots, int y0, int y1) {
        for (int[] spot : spots) {
            column(canvas, spot[0], spot[1], y0, y1);
        }
    }

    static void flare(RoomCanvas canvas, int x0, int z0, int x1, int z1, int y, Half half) {
        canvas.fill(
                RoomShape.box(x0, y, z0 - 1, x1, y, z0 - 1),
                LabyrinthBlocks.tileStairs(Direction.SOUTH, half, StairsShape.STRAIGHT));
        canvas.fill(
                RoomShape.box(x0, y, z1 + 1, x1, y, z1 + 1),
                LabyrinthBlocks.tileStairs(Direction.NORTH, half, StairsShape.STRAIGHT));
        canvas.fill(
                RoomShape.box(x0 - 1, y, z0, x0 - 1, y, z1),
                LabyrinthBlocks.tileStairs(Direction.EAST, half, StairsShape.STRAIGHT));
        canvas.fill(
                RoomShape.box(x1 + 1, y, z0, x1 + 1, y, z1),
                LabyrinthBlocks.tileStairs(Direction.WEST, half, StairsShape.STRAIGHT));
        canvas.set(x0 - 1, y, z0 - 1, LabyrinthBlocks.tileStairs(Direction.SOUTH, half, StairsShape.OUTER_LEFT));
        canvas.set(x1 + 1, y, z0 - 1, LabyrinthBlocks.tileStairs(Direction.SOUTH, half, StairsShape.OUTER_RIGHT));
        canvas.set(x0 - 1, y, z1 + 1, LabyrinthBlocks.tileStairs(Direction.NORTH, half, StairsShape.OUTER_RIGHT));
        canvas.set(x1 + 1, y, z1 + 1, LabyrinthBlocks.tileStairs(Direction.NORTH, half, StairsShape.OUTER_LEFT));
    }

    static void flaredColumn(RoomCanvas canvas, int x0, int z0, int x1, int z1, int y0, int y1) {
        footedColumn(canvas, x0, z0, x1, z1, y0, y1);
        flare(canvas, x0, z0, x1, z1, y1, Half.TOP);
    }

    static void footedColumn(RoomCanvas canvas, int x0, int z0, int x1, int z1, int y0, int y1) {
        column(canvas, x0, z0, x1, z1, y0, y1);
        flare(canvas, x0, z0, x1, z1, y0, Half.BOTTOM);
    }

    static void steps(RoomCanvas canvas, int x0, int x1, int y, int z, Direction up) {
        canvas.fill(
                RoomShape.box(x0, y, z, x1, y, z), LabyrinthBlocks.tileStairs(up, Half.BOTTOM, StairsShape.STRAIGHT));
    }
}
