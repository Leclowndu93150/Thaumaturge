package com.leclowndu93150.thaumaturge.data.labyrinth;

import static com.leclowndu93150.thaumaturge.data.labyrinth.RoomCanvas.F;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.SocketProfile;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthRoomTags;
import java.util.List;
import net.minecraft.core.Direction;

final class CorridorRooms {
    private static final int DEAD_END_DEPTH = 9;

    private CorridorRooms() {}

    static List<RoomRecipe> all() {
        return List.of(
                fallback(
                        RoomRecipe.builder("corridor/dead_end", CorridorRooms::deadEnd)
                                .entrance(),
                        4),
                fallback(
                        RoomRecipe.builder("corridor/straight", Junction.STRAIGHT::carve)
                                .junction(Junction.STRAIGHT),
                        10),
                fallback(
                        RoomRecipe.builder("corridor/bend", CorridorRooms::bend).junction(Junction.BEND), 8),
                fallback(RoomRecipe.builder("corridor/tee", CorridorRooms::tee).junction(Junction.TEE), 6),
                fallback(
                        RoomRecipe.builder("corridor/cross", CorridorRooms::cross)
                                .junction(Junction.CROSS),
                        4));
    }

    private static RoomRecipe fallback(RoomRecipe.Builder builder, int weight) {
        return builder.tags(TTLabyrinthRoomTags.PASSAGES, TTLabyrinthRoomTags.FALLBACK)
                .weight(weight)
                .build();
    }

    static void deadEnd(RoomCanvas canvas) {
        RoomKit.entrance(canvas, DEAD_END_DEPTH);
        canvas.carve(RoomShape.cylinder(RoomKit.MID, 9.0, 3.0, F + 1, F + 5));
        canvas.carve(RoomShape.cylinder(RoomKit.MID, 9.0, 2.0, F + 6, F + 6));
        RoomKit.glyph(canvas, 8, F + 3, 12, Direction.NORTH);
    }

    static void bend(RoomCanvas canvas) {
        Junction.BEND.carve(canvas);
        canvas.carve(RoomShape.cylinder(RoomKit.MID, RoomKit.MID, 3.5, F + 1, F + 5));
    }

    static void tee(RoomCanvas canvas) {
        Junction.TEE.carve(canvas);
        canvas.carve(RoomShape.box(
                SocketProfile.MIN_U, F + 1, SocketProfile.MIN_U, SocketProfile.MAX_U, F + 6, SocketProfile.MAX_U));
    }

    static void cross(RoomCanvas canvas) {
        Junction.CROSS.carve(canvas);
        canvas.carve(RoomShape.octagon(RoomKit.MID, RoomKit.MID, 4.0, 2.0, F + 1, F + 7));
        canvas.fill(RoomShape.octagon(RoomKit.MID, RoomKit.MID, 4.0, 2.0, F, F), LabyrinthBlocks.tile());
    }
}
