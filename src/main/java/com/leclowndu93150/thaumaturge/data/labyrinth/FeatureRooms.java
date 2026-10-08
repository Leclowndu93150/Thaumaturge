package com.leclowndu93150.thaumaturge.data.labyrinth;

import static com.leclowndu93150.thaumaturge.data.labyrinth.RoomCanvas.F;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GuardianPostMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.ReliquaryMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.SpawnerMarker;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthRoomTags;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;

final class FeatureRooms {
    private static final double NEST_DEPTH = 9.5;
    private static final int MAX_PER_MAZE = 2;
    private static final float NEST_LOOT = 0.7F;
    private static final float SPIDER_LOOT = 0.5F;
    private static final float WEB_CHANCE = 0.75F;
    private static final float LIBRARY_LOOT = 0.4F;
    private static final int[][] CRAB_SPAWNERS = {{5, 10}, {10, 10}};
    private static final int[][] GUARDIAN_PILLARS = {{5, 7}, {10, 7}};
    private static final int[][] SPIDER_WEBS = {{4, 8}, {11, 8}, {6, 12}, {9, 13}, {7, 6}};

    private FeatureRooms() {}

    static List<RoomRecipe> all() {
        return List.of(
                feature("nest/crabs", FeatureRooms::crabs, TTLabyrinthRoomTags.NESTS),
                feature("nest/guardians", FeatureRooms::guardians, TTLabyrinthRoomTags.NESTS),
                feature("nest/spiders", FeatureRooms::spiders, TTLabyrinthRoomTags.NESTS),
                feature("library/study", FeatureRooms::study, TTLabyrinthRoomTags.LIBRARIES),
                feature("library/archive", FeatureRooms::archive, TTLabyrinthRoomTags.LIBRARIES),
                feature("library/scriptorium", FeatureRooms::scriptorium, TTLabyrinthRoomTags.LIBRARIES));
    }

    private static RoomRecipe feature(String name, Consumer<RoomCanvas> body, TagKey<RoomType> tag) {
        return RoomRecipe.builder(name, body)
                .entrance()
                .tags(tag)
                .maxPerMaze(MAX_PER_MAZE)
                .build();
    }

    private static ReliquaryMarker cache() {
        return new ReliquaryMarker(Direction.NORTH, Optional.of(TTLootTables.TREASURE_LIBRARY));
    }

    static void crabs(RoomCanvas canvas) {
        RoomKit.entrance(canvas, 5);
        canvas.carve(RoomShape.cylinder(RoomKit.MID, NEST_DEPTH, 5.0, F + 1, F + 5));
        canvas.carve(RoomShape.dome(RoomKit.MID, F + 5, NEST_DEPTH, 5.0, 2.5, 5.0));
        canvas.fill(RoomShape.box(6, F, 8, 9, F, 11), LabyrinthBlocks.glowingCrust());
        for (int[] spawner : CRAB_SPAWNERS) {
            canvas.marker(
                    spawner[0],
                    F,
                    spawner[1],
                    RoomKit.embedded(LabyrinthBlocks.crabSpawner(), 1.0F, LabyrinthBlocks.rock()));
        }
        canvas.marker(7, F + 1, 13, RoomKit.crates(NEST_LOOT));
    }

    static void guardians(RoomCanvas canvas) {
        RoomKit.entrance(canvas, 5);
        canvas.carve(RoomShape.box(3, F + 1, 5, 12, F + 6, 13));
        RoomKit.columns(canvas, GUARDIAN_PILLARS, F + 1, F + 6);
        canvas.marker(7, F + 1, 9, new GuardianPostMarker(Optional.empty(), Optional.empty()));
        canvas.marker(4, F + 1, 12, RoomKit.crates(NEST_LOOT));
        canvas.marker(11, F + 1, 12, RoomKit.crates(NEST_LOOT));
    }

    static void spiders(RoomCanvas canvas) {
        RoomKit.entrance(canvas, 5);
        canvas.carve(RoomShape.cylinder(RoomKit.MID, NEST_DEPTH, 4.5, F + 1, F + 6));
        for (int[] web : SPIDER_WEBS) {
            canvas.marker(web[0], F + 5, web[1], RoomKit.scatter(LabyrinthBlocks.web(), WEB_CHANCE));
        }
        canvas.marker(7, F + 1, 12, new SpawnerMarker(TTEntities.MIND_SPIDER.get()));
        canvas.marker(5, F + 1, 9, RoomKit.urns(SPIDER_LOOT));
    }

    static void study(RoomCanvas canvas) {
        RoomKit.entrance(canvas, 4);
        canvas.carve(RoomShape.box(3, F + 1, 4, 12, F + 5, 13));
        canvas.fill(RoomShape.box(3, F + 1, 13, 12, F + 4, 13), LabyrinthBlocks.bookshelf());
        canvas.fill(RoomShape.box(3, F + 1, 7, 3, F + 4, 12), LabyrinthBlocks.bookshelf());
        canvas.fill(RoomShape.box(12, F + 1, 7, 12, F + 4, 12), LabyrinthBlocks.bookshelf());
        canvas.marker(7, F + 1, 11, cache());
        canvas.marker(5, F + 1, 6, RoomKit.urns(LIBRARY_LOOT));
    }

    static void archive(RoomCanvas canvas) {
        RoomKit.entrance(canvas, 3);
        canvas.carve(RoomShape.box(2, F + 1, 3, 13, F + 6, 14));
        canvas.fill(RoomShape.box(4, F + 1, 6, 4, F + 4, 11), LabyrinthBlocks.bookshelf());
        canvas.fill(RoomShape.box(11, F + 1, 6, 11, F + 4, 11), LabyrinthBlocks.bookshelf());
        canvas.fill(RoomShape.box(2, F + 1, 14, 13, F + 4, 14), LabyrinthBlocks.bookshelf());
        canvas.fill(RoomShape.box(6, F, 12, 9, F, 13), LabyrinthBlocks.tile());
        canvas.marker(7, F + 1, 12, cache());
        canvas.marker(2, F + 1, 4, RoomKit.crates(LIBRARY_LOOT));
    }

    static void scriptorium(RoomCanvas canvas) {
        RoomKit.entrance(canvas, 4);
        canvas.carve(RoomShape.cylinder(RoomKit.MID, NEST_DEPTH, 5.5, F + 1, F + 6));
        canvas.fill(
                RoomShape.annulus(RoomKit.MID, NEST_DEPTH, 5.5, 4.5, F + 1, F + 3)
                        .minus(RoomShape.box(5, F + 1, 3, 10, F + 3, 6)),
                LabyrinthBlocks.bookshelf());
        canvas.fill(RoomShape.cylinder(RoomKit.MID, NEST_DEPTH, 2.0, F, F), LabyrinthBlocks.obsidianTile());
        canvas.marker(7, F + 1, 10, cache());
        canvas.fill(RoomShape.box(6, F + 6, 9, 9, F + 6, 10), LabyrinthBlocks.glowingCrust());
    }
}
