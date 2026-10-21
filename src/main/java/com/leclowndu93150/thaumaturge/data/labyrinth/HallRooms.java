package com.leclowndu93150.thaumaturge.data.labyrinth;

import static com.leclowndu93150.thaumaturge.data.labyrinth.RoomCanvas.F;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LandmarkMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LockMarker;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthRoomTags;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;

final class HallRooms {
    private static final int DOOR_Z = 5;
    private static final int DOOR_HALF_SPAN = 2;
    private static final int LOCK_X = 7;
    private static final int LOCK_Y = F + 3;
    private static final RoomShape DOORWAY = RoomShape.box(
            LOCK_X - DOOR_HALF_SPAN,
            LOCK_Y - DOOR_HALF_SPAN,
            1,
            LOCK_X + DOOR_HALF_SPAN,
            LOCK_Y + DOOR_HALF_SPAN,
            DOOR_Z - 1);
    private static final RoomShape DOOR = RoomShape.box(
            LOCK_X - DOOR_HALF_SPAN,
            LOCK_Y - DOOR_HALF_SPAN,
            DOOR_Z,
            LOCK_X + DOOR_HALF_SPAN,
            LOCK_Y + DOOR_HALF_SPAN,
            DOOR_Z);
    private static final int HALL_MIN_X = 3;
    private static final int HALL_MAX_X = 28;
    private static final int HALL_MIN_Z = 6;
    private static final int HALL_MAX_Z = 29;
    private static final int HALL_TOP = F + 9;
    private static final double HALL_CENTER_X = 16.0;
    private static final double HALL_CENTER_Z = 18.0;
    private static final double NAVE_DOME_RISE = 3.5;
    private static final int NAVE_COLUMN_TOP = HALL_TOP + (int) Math.ceil(NAVE_DOME_RISE);
    private static final int[] NAVE_PILLAR_X = {9, 22};
    private static final int[] NAVE_PILLAR_Z = {11, 17, 23};
    private static final int[][] NAVE_SPAWNS = {{13, 13}, {19, 13}, {13, 25}, {19, 25}};
    private static final int[][] RING_SPAWNS = {{12, 14}, {20, 14}, {12, 22}, {20, 22}};
    private static final int[][] ARENA_SPAWNS = {{10, 12}, {22, 12}, {10, 25}, {22, 25}};
    private static final double RING_RADIUS = 12.0;
    private static final double TIER_INNER = 10.0;
    private static final RoomShape RING_VESTIBULE = RoomShape.box(5, F + 1, DOOR_Z + 1, 11, F + 6, 10);
    private static final int ARENA_TOP = F + 12;

    private HallRooms() {}

    static List<RoomRecipe> all() {
        return List.of(
                hall("hall/nave", HallRooms::nave, TTLabyrinthRoomTags.BOSS_HALLS),
                hall("hall/ring", HallRooms::ring, TTLabyrinthRoomTags.BOSS_HALLS),
                hall(
                        "hall/arena",
                        HallRooms::arena,
                        TTLabyrinthRoomTags.BOSS_HALLS,
                        TTLabyrinthRoomTags.BOSS_HALLS_OPEN));
    }

    @SafeVarargs
    private static RoomRecipe hall(String name, Consumer<RoomCanvas> body, TagKey<RoomType>... tags) {
        return RoomRecipe.builder(name, body)
                .footprint(2, 2)
                .entrance()
                .tags(tags)
                .build();
    }

    static void nave(RoomCanvas canvas) {
        vestibule(canvas);
        canvas.carve(RoomShape.box(HALL_MIN_X, F + 1, HALL_MIN_Z, HALL_MAX_X, HALL_TOP, HALL_MAX_Z));
        canvas.carve(RoomShape.dome(HALL_CENTER_X, HALL_TOP, HALL_CENTER_Z, 13.0, NAVE_DOME_RISE, 12.0));
        canvas.fill(RoomShape.box(HALL_MIN_X, F, HALL_MIN_Z, HALL_MAX_X, F, HALL_MAX_Z), LabyrinthBlocks.inert());
        canvas.fill(RoomShape.annulus(HALL_CENTER_X, HALL_CENTER_Z, 7.0, 6.0, F, F), LabyrinthBlocks.obsidianTile());
        canvas.fill(RoomShape.cylinder(HALL_CENTER_X, HALL_CENTER_Z, 2.0, F, F), LabyrinthBlocks.tile());
        for (int x : NAVE_PILLAR_X) {
            for (int z : NAVE_PILLAR_Z) {
                RoomKit.footedColumn(canvas, x, z, x + 1, z + 1, F + 1, NAVE_COLUMN_TOP);
            }
        }
        landmarks(canvas, NAVE_SPAWNS);
    }

    static void ring(RoomCanvas canvas) {
        vestibule(canvas);
        canvas.carve(RING_VESTIBULE);
        canvas.carve(RoomShape.cylinder(HALL_CENTER_X, HALL_CENTER_Z, RING_RADIUS, F + 1, HALL_TOP));
        canvas.carve(RoomShape.dome(HALL_CENTER_X, HALL_TOP, HALL_CENTER_Z, RING_RADIUS, 4.0, RING_RADIUS));
        canvas.fill(
                RoomShape.annulus(HALL_CENTER_X, HALL_CENTER_Z, RING_RADIUS, TIER_INNER, F + 1, F + 1)
                        .minus(RING_VESTIBULE),
                LabyrinthBlocks.tile());
        canvas.fill(RoomShape.cylinder(HALL_CENTER_X, HALL_CENTER_Z, 4.0, F, F), LabyrinthBlocks.obsidianTile());
        canvas.fill(RoomShape.cylinder(HALL_CENTER_X, HALL_CENTER_Z, 1.5, F, F), LabyrinthBlocks.tile());
        landmarks(canvas, RING_SPAWNS);
    }

    static void arena(RoomCanvas canvas) {
        vestibule(canvas);
        canvas.carve(RoomShape.box(HALL_MIN_X, F + 1, HALL_MIN_Z, HALL_MAX_X, ARENA_TOP, HALL_MAX_Z));
        canvas.carve(RoomShape.dome(HALL_CENTER_X, ARENA_TOP, HALL_CENTER_Z, 13.0, 3.0, 12.0));
        canvas.fill(RoomShape.box(HALL_MIN_X, F, HALL_MIN_Z, HALL_MAX_X, F, HALL_MAX_Z), LabyrinthBlocks.inert());
        canvas.fill(
                RoomShape.octagon(HALL_CENTER_X, HALL_CENTER_Z, 8.0, 3.0, F, F)
                        .minus(RoomShape.octagon(HALL_CENTER_X, HALL_CENTER_Z, 7.0, 3.0, F, F)),
                LabyrinthBlocks.obsidianTile());
        canvas.fill(RoomShape.cylinder(HALL_CENTER_X, HALL_CENTER_Z, 2.0, F, F), LabyrinthBlocks.tile());
        landmarks(canvas, ARENA_SPAWNS);
    }

    private static void vestibule(RoomCanvas canvas) {
        RoomKit.entrance(canvas, 1);
        canvas.carve(DOORWAY);
        canvas.fill(DOOR, LabyrinthBlocks.door());
        canvas.marker(LOCK_X, LOCK_Y, DOOR_Z, new LockMarker(Direction.NORTH));
    }

    private static void landmarks(RoomCanvas canvas, int[][] spawns) {
        int centerX = (int) HALL_CENTER_X;
        int centerZ = (int) HALL_CENTER_Z;
        canvas.marker(centerX, F + 1, centerZ, new LandmarkMarker(LabyrinthLandmarks.BOSS_CENTER));
        canvas.marker(centerX, F + 1, centerZ + 3, new LandmarkMarker(LabyrinthLandmarks.REWARD));
        canvas.marker(centerX, F + 1, HALL_MAX_Z - 2, new LandmarkMarker(LabyrinthLandmarks.EXIT));
        for (int i = 0; i < spawns.length; i++) {
            canvas.marker(
                    spawns[i][0],
                    F + 1,
                    spawns[i][1],
                    new LandmarkMarker(TTIds.rl(LabyrinthLandmarks.SPAWN_PREFIX + i)));
        }
    }
}
