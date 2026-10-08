package com.leclowndu93150.thaumaturge.data.labyrinth;

import static com.leclowndu93150.thaumaturge.data.labyrinth.RoomCanvas.F;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.EntryPortalMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.KeyReliquaryMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LandmarkMarker;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthRoomTags;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

final class SanctumRooms {
    private static final int PORTAL_X = 7;
    private static final int PORTAL_Z = 7;
    private static final int SHAFT_TOP = F + 15;
    private static final int[][] SHAFTS = {{4, 4}, {10, 4}, {4, 10}, {10, 10}};
    private static final int[][] RING_PILLARS = {{3, 3}, {12, 3}, {3, 12}, {12, 12}};
    private static final double RING_RADIUS = 6.5;

    private SanctumRooms() {}

    static List<RoomRecipe> all() {
        return List.of(
                RoomRecipe.builder("sanctum/portal", SanctumRooms::portal)
                        .junction(Junction.CROSS)
                        .tags(TTLabyrinthRoomTags.PORTAL)
                        .plain()
                        .build(),
                RoomRecipe.builder("reliquary/key", SanctumRooms::key)
                        .entrance()
                        .tags(TTLabyrinthRoomTags.KEY)
                        .plain()
                        .build(),
                RoomRecipe.builder("reliquary/vault", SanctumRooms::vault)
                        .entrance()
                        .tags(TTLabyrinthRoomTags.KEY)
                        .plain()
                        .build(),
                RoomRecipe.builder("sanctum/ring", SanctumRooms::ring)
                        .junction(Junction.CROSS)
                        .tags(TTLabyrinthRoomTags.PORTAL)
                        .plain()
                        .build());
    }

    static void portal(RoomCanvas canvas) {
        Junction.CROSS.carve(canvas);
        canvas.carve(RoomShape.octagon(RoomKit.MID, RoomKit.MID, 6.0, 4.0, F + 1, F + 9));
        canvas.carve(RoomShape.dome(RoomKit.MID, F + 9, RoomKit.MID, 5.5, 3.5, 5.5));
        for (int[] shaft : SHAFTS) {
            canvas.carve(RoomShape.box(shaft[0], F + 10, shaft[1], shaft[0] + 1, SHAFT_TOP - 1, shaft[1] + 1));
            canvas.fill(
                    RoomShape.box(shaft[0], SHAFT_TOP, shaft[1], shaft[0] + 1, SHAFT_TOP, shaft[1] + 1),
                    LabyrinthBlocks.starfield());
        }
        canvas.fill(RoomShape.octagon(RoomKit.MID, RoomKit.MID, 6.0, 4.0, F, F), LabyrinthBlocks.tile());
        portalAltar(canvas, LabyrinthBlocks.obsidianTile(), F + 8);
        RoomKit.glyph(canvas, 4, F + 3, 1, Direction.EAST);
        RoomKit.glyph(canvas, 11, F + 3, 14, Direction.WEST);
        RoomKit.glyph(canvas, 1, F + 3, 4, Direction.SOUTH);
        RoomKit.glyph(canvas, 14, F + 3, 11, Direction.NORTH);
    }

    static void key(RoomCanvas canvas) {
        RoomKit.entrance(canvas, 5);
        canvas.carve(RoomShape.box(3, F + 1, 5, 12, F + 6, 13));
        canvas.carve(RoomShape.dome(RoomKit.MID, F + 6, 9.5, 5.0, 2.5, 4.5));
        canvas.fill(RoomShape.box(3, F, 5, 12, F, 13), LabyrinthBlocks.inert());
        canvas.fill(RoomShape.box(6, F + 1, 10, 9, F + 1, 12), LabyrinthBlocks.tile());
        RoomKit.steps(canvas, 6, 9, F + 1, 9, Direction.SOUTH);
        canvas.marker(7, F + 2, 11, new KeyReliquaryMarker(Direction.NORTH));
        canvas.marker(4, F + 1, 7, RoomKit.keyWard());
        canvas.marker(11, F + 1, 7, RoomKit.keyWard());
        RoomKit.glyph(canvas, 5, F + 3, 14, Direction.NORTH);
        RoomKit.glyph(canvas, 10, F + 3, 14, Direction.NORTH);
    }

    static void vault(RoomCanvas canvas) {
        RoomKit.entrance(canvas, 4);
        canvas.carve(RoomShape.box(4, F + 1, 4, 11, F + 6, 14));
        canvas.carve(RoomShape.dome(RoomKit.MID, F + 6, 9.0, 3.5, 2.5, 5.0));
        canvas.fill(RoomShape.box(4, F, 4, 11, F, 14), LabyrinthBlocks.inert());
        canvas.fill(RoomShape.box(5, F + 1, 11, 10, F + 1, 13), LabyrinthBlocks.tile());
        RoomKit.steps(canvas, 5, 10, F + 1, 10, Direction.SOUTH);
        RoomKit.column(canvas, 4, 10, F + 1, F + 6);
        RoomKit.column(canvas, 11, 10, F + 1, F + 6);
        canvas.marker(7, F + 2, 12, new KeyReliquaryMarker(Direction.NORTH));
        canvas.marker(5, F + 1, 6, RoomKit.keyWard());
        canvas.marker(10, F + 1, 6, RoomKit.keyWard());
        RoomKit.glyph(canvas, 3, F + 3, 8, Direction.EAST);
        RoomKit.glyph(canvas, 12, F + 3, 8, Direction.WEST);
    }

    static void ring(RoomCanvas canvas) {
        Junction.CROSS.carve(canvas);
        canvas.carve(RoomShape.cylinder(RoomKit.MID, RoomKit.MID, RING_RADIUS, F + 1, F + 8));
        canvas.carve(RoomShape.dome(RoomKit.MID, F + 8, RoomKit.MID, 6.0, 4.0, 6.0));
        canvas.fill(
                RoomShape.annulus(RoomKit.MID, RoomKit.MID, RING_RADIUS, 5.0, F, F), LabyrinthBlocks.obsidianTile());
        for (int[] pillar : RING_PILLARS) {
            RoomKit.column(canvas, pillar[0], pillar[1], F + 1, F + 8);
            RoomKit.glyph(
                    canvas, pillar[0], F + 3, pillar[1], pillar[0] < RoomKit.MID ? Direction.EAST : Direction.WEST);
        }
        portalAltar(canvas, LabyrinthBlocks.tile(), F + 10);
    }

    private static void portalAltar(RoomCanvas canvas, BlockState plinth, int pillarTop) {
        canvas.fill(RoomShape.box(PORTAL_X - 1, F, PORTAL_Z - 1, PORTAL_X + 1, F, PORTAL_Z + 1), plinth);
        canvas.set(PORTAL_X, F, PORTAL_Z, LabyrinthBlocks.capstone());
        canvas.marker(PORTAL_X, F + 1, PORTAL_Z, EntryPortalMarker.INSTANCE);
        canvas.set(PORTAL_X, F + 3, PORTAL_Z, LabyrinthBlocks.obelisk());
        canvas.fill(RoomShape.box(PORTAL_X, F + 4, PORTAL_Z, PORTAL_X, pillarTop, PORTAL_Z), LabyrinthBlocks.pillar());
        canvas.marker(PORTAL_X, F + 1, PORTAL_Z + 4, new LandmarkMarker(LabyrinthLandmarks.ARRIVAL));
    }
}
