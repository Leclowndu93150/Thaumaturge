package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.guardian.GuardianPosts;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.BlockMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GlyphMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GuardianPostMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LootMarker;
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
        column(canvas, x0, z0, x1, z1, y0, y1);
        flare(canvas, x0, z0, x1, z1, y0, Half.BOTTOM);
        flare(canvas, x0, z0, x1, z1, y1, Half.TOP);
    }

    static void steps(RoomCanvas canvas, int x0, int x1, int y, int z, Direction up) {
        canvas.fill(
                RoomShape.box(x0, y, z, x1, y, z), LabyrinthBlocks.tileStairs(up, Half.BOTTOM, StairsShape.STRAIGHT));
    }
}
