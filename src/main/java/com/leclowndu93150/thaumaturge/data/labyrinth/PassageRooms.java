package com.leclowndu93150.thaumaturge.data.labyrinth;

import static com.leclowndu93150.thaumaturge.data.labyrinth.RoomCanvas.F;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.SpawnerMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.SocketProfile;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthRoomTags;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

final class PassageRooms {
    private static final int RARE_WEIGHT = 2;
    private static final int RARE_MAX = 2;
    private static final float ALCOVE_LOOT = 0.6F;
    private static final float CRYPT_LOOT = 0.4F;
    private static final float SHRINE_LOOT = 0.7F;
    private static final float TRAP_CHANCE = 0.6F;
    private static final float WEB_CHANCE = 0.7F;
    private static final float SIDE_LOOT = 0.3F;
    private static final float BAIT_LOOT = 0.5F;
    private static final float RUIN_LOOT = 0.4F;
    private static final float VENT_CHANCE = 0.15F;
    private static final float LAIR_VENT_CHANCE = 0.25F;
    private static final float WARREN_FLOOR_VENT = 0.35F;
    private static final float WARREN_WALL_VENT = 0.3F;
    private static final float WARREN_LOOT = 0.5F;
    private static final float FLOOR_TRAP_CHANCE = 0.15F;
    private static final int[][] WARREN_FLOOR_VENTS = {{6, 3}, {9, 8}, {7, 12}};
    private static final int[][] WARREN_WALL_VENTS = {{4, 5}, {11, 10}};
    private static final int[] RIBS = {3, 7, 11};
    private static final int[] CRYPT_NICHES = {2, 6, 10};
    private static final int[][] STRAIGHT_TRAPS = {{7, 3}, {8, 6}, {6, 9}, {9, 12}};
    private static final int[][] BEND_TRAPS = {{7, 3}, {8, 7}, {12, 7}};
    private static final int[] WEB_ROWS = {2, 5, 9, 12};

    private PassageRooms() {}

    static List<RoomRecipe> all() {
        return List.of(
                passage("passage/ribbed", PassageRooms::ribbed, Junction.STRAIGHT, 6),
                passage("passage/vaulted", PassageRooms::vaulted, Junction.STRAIGHT, 4),
                passage("passage/pillared", PassageRooms::pillared, Junction.STRAIGHT, 4),
                passage("passage/sunken", PassageRooms::sunken, Junction.STRAIGHT, 3),
                passage("passage/alcoves", PassageRooms::alcoves, Junction.STRAIGHT, 3),
                passage("passage/crypt", PassageRooms::crypt, Junction.STRAIGHT, 2),
                passage("passage/warren", PassageRooms::warren, Junction.STRAIGHT, 2),
                passage("bend/rounded", PassageRooms::rounded, Junction.BEND, 4),
                passage("bend/pillar", PassageRooms::pillarBend, Junction.BEND, 3),
                passage("bend/shrine", PassageRooms::shrineBend, Junction.BEND, 2),
                passage("bend/glyphs", PassageRooms::glyphBend, Junction.BEND, 3),
                rare("rare/trapped_straight", PassageRooms::trappedStraight, Junction.STRAIGHT, Optional.empty()),
                rare("rare/trapped_bend", PassageRooms::trappedBend, Junction.BEND, Optional.empty()),
                rare(
                        "rare/crumbling_straight",
                        PassageRooms::crumblingStraight,
                        Junction.STRAIGHT,
                        Optional.of(LabyrinthProcessorBootstrap.CRUMBLING)),
                rare(
                        "rare/crumbling_bend",
                        PassageRooms::crumblingBend,
                        Junction.BEND,
                        Optional.of(LabyrinthProcessorBootstrap.CRUMBLING)),
                rare(
                        "rare/weeping_straight",
                        PassageRooms::weepingStraight,
                        Junction.STRAIGHT,
                        Optional.of(LabyrinthProcessorBootstrap.WEEPING)),
                rare(
                        "rare/weeping_bend",
                        PassageRooms::weepingBend,
                        Junction.BEND,
                        Optional.of(LabyrinthProcessorBootstrap.WEEPING)),
                rare("rare/webbed_straight", PassageRooms::webbedStraight, Junction.STRAIGHT, Optional.empty()),
                rare("rare/webbed_bend", PassageRooms::webbedBend, Junction.BEND, Optional.empty()));
    }

    private static RoomRecipe passage(String name, Consumer<RoomCanvas> body, Junction junction, int weight) {
        return RoomRecipe.builder(name, body)
                .junction(junction)
                .tags(TTLabyrinthRoomTags.PASSAGES)
                .weight(weight)
                .variety(junction == Junction.BEND ? "bend" : "straight")
                .build();
    }

    private static RoomRecipe rare(
            String name,
            Consumer<RoomCanvas> body,
            Junction junction,
            Optional<ResourceKey<StructureProcessorList>> processors) {
        RoomRecipe.Builder builder = RoomRecipe.builder(name, body)
                .junction(junction)
                .tags(TTLabyrinthRoomTags.RARE)
                .weight(RARE_WEIGHT)
                .maxPerMaze(RARE_MAX);
        processors.ifPresent(builder::processors);
        return builder.build();
    }

    static void ribbed(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        for (int z : RIBS) {
            RoomKit.column(canvas, SocketProfile.MIN_U, z, F + 1, F + 5);
            RoomKit.column(canvas, SocketProfile.MAX_U, z, F + 1, F + 5);
            canvas.fill(
                    RoomShape.box(SocketProfile.CROWN_MIN_U, F + 6, z, SocketProfile.CROWN_MAX_U, F + 6, z),
                    LabyrinthBlocks.column(Direction.Axis.X));
            canvas.set(
                    SocketProfile.CROWN_MIN_U,
                    F + 5,
                    z,
                    LabyrinthBlocks.tileStairs(Direction.WEST, Half.TOP, StairsShape.STRAIGHT));
            canvas.set(
                    SocketProfile.CROWN_MAX_U,
                    F + 5,
                    z,
                    LabyrinthBlocks.tileStairs(Direction.EAST, Half.TOP, StairsShape.STRAIGHT));
        }
        RoomKit.niche(canvas, 4, 5, RoomKit.urns(SIDE_LOOT));
        RoomKit.niche(canvas, 11, 9, RoomKit.urns(SIDE_LOOT));
        RoomKit.floorVent(canvas, 7, 9, VENT_CHANCE);
        RoomKit.wallVent(canvas, 4, 13, VENT_CHANCE);
    }

    static void vaulted(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        canvas.carve(RoomShape.box(4, F + 1, 2, 11, F + 5, 13));
        canvas.carve(RoomShape.dome(RoomKit.MID, F + 5, RoomKit.MID, 4.5, 4.0, 6.5));
        canvas.fill(RoomShape.box(7, F, 2, 8, F, 13), LabyrinthBlocks.obsidianTile());
        RoomKit.glyph(canvas, 3, F + 3, 7, Direction.EAST);
        RoomKit.glyph(canvas, 12, F + 3, 8, Direction.WEST);
        RoomKit.floorLoot(canvas, 4, 12, RoomKit.urns(SIDE_LOOT));
        RoomKit.plinth(canvas, 10, 4, RoomKit.urns(SIDE_LOOT));
        RoomKit.floorVent(canvas, 5, 8, VENT_CHANCE);
        RoomKit.wallVent(canvas, 3, 10, VENT_CHANCE);
    }

    static void pillared(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        canvas.carve(RoomShape.box(3, F + 1, 2, 12, F + 6, 13));
        for (int z : RIBS) {
            RoomKit.column(canvas, 4, z, F + 1, F + 6);
            RoomKit.column(canvas, 11, z, F + 1, F + 6);
        }
        RoomKit.floorLoot(canvas, 3, 5, RoomKit.urns(SIDE_LOOT));
        RoomKit.floorLoot(canvas, 12, 9, RoomKit.urns(SIDE_LOOT));
        RoomKit.wallVent(canvas, 2, 9, VENT_CHANCE);
        RoomKit.floorTrap(canvas, 7, 5, FLOOR_TRAP_CHANCE);
    }

    static void sunken(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        canvas.fill(RoomShape.box(6, F, 1, 9, F, 14), LabyrinthBlocks.tile());
        canvas.fill(RoomShape.box(7, F, 1, 8, F, 14), LabyrinthBlocks.obsidianTile());
        RoomKit.glyph(canvas, 4, F + 3, 8, Direction.EAST);
        RoomKit.floorLoot(canvas, 5, 4, RoomKit.urns(SIDE_LOOT));
        RoomKit.floorLoot(canvas, 10, 11, RoomKit.urns(SIDE_LOOT));
        RoomKit.wallVent(canvas, 11, 6, VENT_CHANCE);
    }

    static void alcoves(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        canvas.carve(RoomShape.box(2, F + 1, 4, 4, F + 4, 6));
        canvas.carve(RoomShape.box(11, F + 1, 9, 13, F + 4, 11));
        canvas.marker(3, F + 1, 5, RoomKit.urns(ALCOVE_LOOT));
        canvas.marker(12, F + 1, 10, RoomKit.urns(ALCOVE_LOOT));
        RoomKit.glyph(canvas, 1, F + 3, 5, Direction.EAST);
        RoomKit.glyph(canvas, 14, F + 3, 10, Direction.WEST);
        RoomKit.wallVent(canvas, 3, 3, LAIR_VENT_CHANCE);
        RoomKit.wallVent(canvas, 12, 12, LAIR_VENT_CHANCE);
        RoomKit.floorTrap(canvas, 7, 8, FLOOR_TRAP_CHANCE);
    }

    static void crypt(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        for (int z : CRYPT_NICHES) {
            canvas.carve(RoomShape.box(3, F + 1, z, 4, F + 3, z + 2));
            canvas.carve(RoomShape.box(11, F + 1, z, 12, F + 3, z + 2));
            canvas.fill(RoomShape.box(3, F + 1, z, 3, F + 1, z + 2), LabyrinthBlocks.obsidianTile());
            canvas.fill(RoomShape.box(12, F + 1, z, 12, F + 1, z + 2), LabyrinthBlocks.obsidianTile());
        }
        canvas.marker(4, F + 1, 7, RoomKit.urns(CRYPT_LOOT));
        canvas.marker(11, F + 1, 11, RoomKit.urns(CRYPT_LOOT));
        RoomKit.floorLoot(canvas, 11, 3, RoomKit.urns(CRYPT_LOOT));
        RoomKit.floorLoot(canvas, 4, 11, RoomKit.urns(CRYPT_LOOT));
        RoomKit.floorVent(canvas, 7, 9, LAIR_VENT_CHANCE);
    }

    static void warren(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        canvas.fill(RoomShape.box(5, F, 2, 10, F, 13), LabyrinthBlocks.crust());
        for (int[] vent : WARREN_FLOOR_VENTS) {
            RoomKit.floorVent(canvas, vent[0], vent[1], WARREN_FLOOR_VENT);
        }
        for (int[] vent : WARREN_WALL_VENTS) {
            RoomKit.wallVent(canvas, vent[0], vent[1], WARREN_WALL_VENT);
        }
        RoomKit.niche(canvas, 11, 3, RoomKit.crates(WARREN_LOOT));
        canvas.fill(RoomShape.box(7, F + 6, 6, 8, F + 6, 9), LabyrinthBlocks.glowingCrust());
    }

    static void rounded(RoomCanvas canvas) {
        canvas.run(Direction.Axis.Z, 0, 0, 6);
        canvas.run(Direction.Axis.X, 0, 9, SocketProfile.LAST);
        canvas.carve(RoomShape.cylinder(RoomKit.MID, RoomKit.MID, 4.5, F + 1, F + 6));
        canvas.fill(RoomShape.cylinder(RoomKit.MID, RoomKit.MID, 2.0, F, F), LabyrinthBlocks.tile());
        RoomKit.floorLoot(canvas, 5, 11, RoomKit.urns(SIDE_LOOT));
        RoomKit.floorVent(canvas, 6, 9, VENT_CHANCE);
        RoomKit.wallVent(canvas, 3, 8, VENT_CHANCE);
    }

    static void pillarBend(RoomCanvas canvas) {
        Junction.BEND.carve(canvas);
        canvas.carve(RoomShape.box(4, F + 1, 4, 11, F + 6, 11));
        RoomKit.flaredColumn(canvas, 7, 7, 8, 8, F + 1, F + 6);
        RoomKit.floorLoot(canvas, 4, 11, RoomKit.urns(SIDE_LOOT));
        RoomKit.floorLoot(canvas, 11, 4, RoomKit.urns(SIDE_LOOT));
        RoomKit.floorVent(canvas, 5, 5, VENT_CHANCE);
        RoomKit.wallVent(canvas, 3, 7, VENT_CHANCE);
    }

    static void shrineBend(RoomCanvas canvas) {
        Junction.BEND.carve(canvas);
        canvas.carve(RoomShape.box(3, F + 1, 11, 5, F + 4, 13));
        canvas.fill(RoomShape.box(3, F, 11, 5, F, 13), LabyrinthBlocks.obsidianTile());
        canvas.marker(4, F + 1, 12, RoomKit.crates(SHRINE_LOOT));
        RoomKit.glyph(canvas, 4, F + 3, 14, Direction.NORTH);
        RoomKit.wallVent(canvas, 4, 2, LAIR_VENT_CHANCE);
        RoomKit.floorTrap(canvas, 8, 3, FLOOR_TRAP_CHANCE);
    }

    static void glyphBend(RoomCanvas canvas) {
        Junction.BEND.carve(canvas);
        RoomKit.glyph(canvas, 4, F + 3, 3, Direction.EAST);
        RoomKit.glyph(canvas, 4, F + 3, 8, Direction.EAST);
        RoomKit.glyph(canvas, 8, F + 3, 11, Direction.NORTH);
        canvas.fill(RoomShape.box(5, F + 6, 6, 9, F + 6, 9), LabyrinthBlocks.glowingCrust());
        RoomKit.niche(canvas, 4, 6, RoomKit.urns(SIDE_LOOT));
        RoomKit.floorVent(canvas, 8, 7, VENT_CHANCE);
    }

    static void trappedStraight(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        traps(canvas, STRAIGHT_TRAPS);
        RoomKit.niche(canvas, 11, 7, RoomKit.crates(BAIT_LOOT));
    }

    static void trappedBend(RoomCanvas canvas) {
        Junction.BEND.carve(canvas);
        traps(canvas, BEND_TRAPS);
        RoomKit.niche(canvas, 4, 8, RoomKit.crates(BAIT_LOOT));
    }

    private static void traps(RoomCanvas canvas, int[][] spots) {
        for (int[] spot : spots) {
            RoomKit.floorTrap(canvas, spot[0], spot[1], TRAP_CHANCE);
        }
    }

    static void crumblingStraight(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        rubble(canvas, LabyrinthBlocks.rock());
        RoomKit.niche(canvas, 11, 12, RoomKit.urns(RUIN_LOOT));
        RoomKit.floorVent(canvas, 7, 10, LAIR_VENT_CHANCE);
    }

    static void crumblingBend(RoomCanvas canvas) {
        Junction.BEND.carve(canvas);
        canvas.fill(RoomShape.box(5, F + 1, 3, 5, F + 1, 5), LabyrinthBlocks.rock());
        canvas.fill(RoomShape.box(9, F + 1, 9, 10, F + 2, 10), LabyrinthBlocks.rock());
        RoomKit.niche(canvas, 4, 8, RoomKit.urns(RUIN_LOOT));
        RoomKit.wallVent(canvas, 8, 11, LAIR_VENT_CHANCE);
    }

    static void weepingStraight(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        rubble(canvas, LabyrinthBlocks.obsidianTile());
        RoomKit.niche(canvas, 11, 3, RoomKit.urns(RUIN_LOOT));
        RoomKit.wallVent(canvas, 4, 9, LAIR_VENT_CHANCE);
    }

    static void weepingBend(RoomCanvas canvas) {
        Junction.BEND.carve(canvas);
        canvas.fill(RoomShape.box(5, F + 1, 3, 5, F + 1, 4), LabyrinthBlocks.obsidianTile());
        canvas.fill(RoomShape.box(10, F + 1, 9, 10, F + 1, 10), LabyrinthBlocks.obsidianTile());
        RoomKit.niche(canvas, 4, 8, RoomKit.urns(RUIN_LOOT));
    }

    private static void rubble(RoomCanvas canvas, BlockState block) {
        canvas.fill(RoomShape.box(5, F + 1, 4, 5, F + 1, 6), block);
        canvas.fill(RoomShape.box(10, F + 1, 9, 10, F + 2, 11), block);
        canvas.fill(RoomShape.box(6, F + 5, 7, 7, F + 6, 8), block);
    }

    static void webbedStraight(RoomCanvas canvas) {
        Junction.STRAIGHT.carve(canvas);
        for (int z : WEB_ROWS) {
            canvas.marker(5, F + 4, z, RoomKit.scatter(LabyrinthBlocks.web(), WEB_CHANCE));
            canvas.marker(10, F + 4, z + 1, RoomKit.scatter(LabyrinthBlocks.web(), WEB_CHANCE));
        }
        canvas.carve(RoomShape.box(11, F + 1, 7, 12, F + 2, 8));
        canvas.marker(12, F + 1, 7, new SpawnerMarker(TTEntities.MIND_SPIDER.get()));
        RoomKit.floorLoot(canvas, 11, 8, RoomKit.urns(RUIN_LOOT));
    }

    static void webbedBend(RoomCanvas canvas) {
        Junction.BEND.carve(canvas);
        canvas.marker(5, F + 4, 2, RoomKit.scatter(LabyrinthBlocks.web(), WEB_CHANCE));
        canvas.marker(10, F + 4, 6, RoomKit.scatter(LabyrinthBlocks.web(), WEB_CHANCE));
        canvas.marker(13, F + 4, 9, RoomKit.scatter(LabyrinthBlocks.web(), WEB_CHANCE));
        canvas.carve(RoomShape.box(3, F + 1, 7, 4, F + 2, 8));
        canvas.marker(3, F + 1, 7, new SpawnerMarker(TTEntities.MIND_SPIDER.get()));
        RoomKit.floorLoot(canvas, 3, 8, RoomKit.urns(RUIN_LOOT));
    }
}
