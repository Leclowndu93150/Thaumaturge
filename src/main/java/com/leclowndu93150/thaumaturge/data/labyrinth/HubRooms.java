package com.leclowndu93150.thaumaturge.data.labyrinth;

import static com.leclowndu93150.thaumaturge.data.labyrinth.RoomCanvas.F;

import com.leclowndu93150.thaumaturge.registry.TTLabyrinthRoomTags;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Half;

final class HubRooms {
    private static final int DEAD_END_DEPTH = 5;
    private static final float URN_LOOT = 0.5F;
    private static final float SHRINE_LOOT = 0.8F;
    private static final float TEE_LOOT = 0.6F;
    private static final int[][] CORNER_PILLARS = {{4, 4}, {11, 4}, {4, 11}, {11, 11}};
    private static final int[][] DEAD_END_URNS = {{5, 10}, {10, 10}, {7, 12}};

    private HubRooms() {}

    static List<RoomRecipe> all() {
        return List.of(
                junction("tee/hall", HubRooms::teeHall, Junction.TEE, 3),
                junction("tee/pillared", HubRooms::teePillared, Junction.TEE, 3),
                junction("tee/shrine", HubRooms::teeShrine, Junction.TEE, 2),
                junction("cross/rotunda", HubRooms::rotunda, Junction.CROSS, 2),
                junction("cross/pit", HubRooms::pit, Junction.CROSS, 1),
                junction("cross/gallery", HubRooms::gallery, Junction.CROSS, 2),
                deadEnd("dead_end/urns", HubRooms::urns, 3),
                deadEnd("dead_end/shrine", HubRooms::shrine, 2),
                deadEnd("dead_end/collapsed", HubRooms::collapsed, 2));
    }

    private static RoomRecipe junction(String name, Consumer<RoomCanvas> body, Junction junction, int weight) {
        return RoomRecipe.builder(name, body)
                .junction(junction)
                .tags(TTLabyrinthRoomTags.PASSAGES)
                .weight(weight)
                .variety(junction == Junction.TEE ? "tee" : "cross")
                .build();
    }

    private static RoomRecipe deadEnd(String name, Consumer<RoomCanvas> body, int weight) {
        return RoomRecipe.builder(name, body)
                .entrance()
                .tags(TTLabyrinthRoomTags.PASSAGES)
                .weight(weight)
                .variety("dead_end")
                .build();
    }

    static void teeHall(RoomCanvas canvas) {
        Junction.TEE.carve(canvas);
        canvas.carve(RoomShape.box(2, F + 1, 3, 13, F + 6, 12));
        canvas.carve(RoomShape.dome(RoomKit.MID, F + 6, RoomKit.MID, 6.0, 3.0, 5.0));
        canvas.fill(RoomShape.box(4, F, 5, 11, F, 10), LabyrinthBlocks.tile());
    }

    static void teePillared(RoomCanvas canvas) {
        Junction.TEE.carve(canvas);
        canvas.carve(RoomShape.box(3, F + 1, 3, 12, F + 6, 12));
        RoomKit.columns(canvas, CORNER_PILLARS, F + 1, F + 6);
    }

    static void teeShrine(RoomCanvas canvas) {
        Junction.TEE.carve(canvas);
        canvas.carve(RoomShape.box(6, F + 1, 11, 9, F + 4, 13));
        canvas.fill(RoomShape.box(6, F, 11, 9, F, 13), LabyrinthBlocks.obsidianTile());
        canvas.marker(7, F + 1, 12, RoomKit.urns(TEE_LOOT));
        RoomKit.glyph(canvas, 7, F + 3, 14, Direction.NORTH);
    }

    static void rotunda(RoomCanvas canvas) {
        Junction.CROSS.carve(canvas);
        canvas.carve(RoomShape.cylinder(RoomKit.MID, RoomKit.MID, 6.0, F + 1, F + 7));
        canvas.carve(RoomShape.dome(RoomKit.MID, F + 7, RoomKit.MID, 6.0, 4.0, 6.0));
        RoomKit.column(canvas, 7, 7, 8, 8, F + 1, F + 12);
        RoomKit.flare(canvas, 7, 7, 8, 8, F + 1, Half.BOTTOM);
        canvas.fill(RoomShape.annulus(RoomKit.MID, RoomKit.MID, 6.0, 5.0, F, F), LabyrinthBlocks.obsidianTile());
    }

    static void pit(RoomCanvas canvas) {
        Junction.CROSS.carve(canvas);
        canvas.carve(RoomShape.octagon(RoomKit.MID, RoomKit.MID, 5.0, 2.0, F + 1, F + 6));
        canvas.carve(RoomShape.box(6, F, 6, 9, F, 9));
        canvas.fill(RoomShape.box(6, F - 1, 6, 9, F - 1, 9), LabyrinthBlocks.starfield());
        canvas.fill(RoomShape.box(5, F, 5, 10, F, 5), LabyrinthBlocks.obsidianTile());
        canvas.fill(RoomShape.box(5, F, 10, 10, F, 10), LabyrinthBlocks.obsidianTile());
    }

    static void gallery(RoomCanvas canvas) {
        Junction.CROSS.carve(canvas);
        canvas.carve(RoomShape.box(2, F + 1, 2, 13, F + 6, 13));
        RoomKit.columns(canvas, CORNER_PILLARS, F + 1, F + 6);
        canvas.fill(RoomShape.box(5, F, 5, 10, F, 10), LabyrinthBlocks.tile());
        RoomKit.glyph(canvas, 1, F + 3, 3, Direction.EAST);
        RoomKit.glyph(canvas, 14, F + 3, 12, Direction.WEST);
    }

    static void urns(RoomCanvas canvas) {
        RoomKit.entrance(canvas, DEAD_END_DEPTH);
        canvas.carve(RoomShape.cylinder(RoomKit.MID, 9.0, 4.0, F + 1, F + 5));
        canvas.carve(RoomShape.cylinder(RoomKit.MID, 9.0, 3.0, F + 6, F + 6));
        for (int[] urn : DEAD_END_URNS) {
            canvas.marker(urn[0], F + 1, urn[1], RoomKit.urns(URN_LOOT));
        }
    }

    static void shrine(RoomCanvas canvas) {
        RoomKit.entrance(canvas, DEAD_END_DEPTH);
        canvas.carve(RoomShape.box(4, F + 1, 5, 11, F + 6, 12));
        canvas.fill(RoomShape.box(6, F + 1, 10, 9, F + 1, 12), LabyrinthBlocks.obsidianTile());
        canvas.marker(7, F + 2, 11, RoomKit.crates(SHRINE_LOOT));
        RoomKit.glyph(canvas, 7, F + 4, 13, Direction.NORTH);
        RoomKit.glyph(canvas, 8, F + 4, 13, Direction.NORTH);
    }

    static void collapsed(RoomCanvas canvas) {
        RoomKit.entrance(canvas, DEAD_END_DEPTH);
        canvas.carve(RoomShape.box(4, F + 1, 5, 11, F + 6, 13));
        canvas.fill(RoomShape.box(4, F + 1, 10, 11, F + 1, 13), LabyrinthBlocks.rock());
        canvas.fill(RoomShape.box(4, F + 1, 9, 11, F + 1, 9), LabyrinthBlocks.rockStairs(Direction.SOUTH));
        canvas.fill(RoomShape.box(4, F + 2, 12, 11, F + 2, 13), LabyrinthBlocks.rock());
        canvas.fill(RoomShape.box(4, F + 2, 11, 11, F + 2, 11), LabyrinthBlocks.rockStairs(Direction.SOUTH));
        canvas.fill(RoomShape.box(5, F + 3, 13, 10, F + 3, 13), LabyrinthBlocks.rockStairs(Direction.SOUTH));
        canvas.marker(7, F + 3, 12, RoomKit.urns(URN_LOOT));
    }
}
