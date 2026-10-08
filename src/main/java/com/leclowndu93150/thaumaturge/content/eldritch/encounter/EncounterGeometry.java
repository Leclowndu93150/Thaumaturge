package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

final class EncounterGeometry {
    private static final int HALL_MARGIN = 1;

    private EncounterGeometry() {}

    static BlockPos anchor(MazeRecord record) {
        return record.plan()
                .landmarks()
                .point(LabyrinthLandmarks.BOSS_CENTER)
                .orElseGet(() -> record.plan().landmarks().bossHall().getCenter());
    }

    static List<BlockPos> spawnPoints(MazeRecord record) {
        List<Map.Entry<ResourceLocation, BlockPos>> entries = new ArrayList<>();
        for (Map.Entry<ResourceLocation, BlockPos> entry :
                record.plan().landmarks().points().entrySet()) {
            if (entry.getKey().getPath().startsWith(LabyrinthLandmarks.SPAWN_PREFIX)) {
                entries.add(entry);
            }
        }
        entries.sort(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)));
        List<BlockPos> points = new ArrayList<>(entries.size());
        for (Map.Entry<ResourceLocation, BlockPos> entry : entries) {
            points.add(entry.getValue());
        }
        return points;
    }

    static BoundingBox hall(MazeRecord record) {
        return record.plan().landmarks().bossHall().inflatedBy(HALL_MARGIN);
    }

    static List<BlockPos> spawnPointsOrAnchor(MazeRecord record) {
        List<BlockPos> points = spawnPoints(record);
        return points.isEmpty() ? List.of(anchor(record)) : points;
    }

    static List<ServerPlayer> hallPlayers(ServerLevel level, MazeRecord record) {
        return players(level, hall(record), player -> player.isAlive() && !player.isSpectator());
    }

    static List<UUID> mazePlayers(ServerLevel level, MazeRecord record) {
        return players(level, record.plan().geometry().bounds(), player -> !player.isSpectator()).stream()
                .map(ServerPlayer::getUUID)
                .toList();
    }

    static boolean occupied(ServerLevel level, MazeRecord record) {
        BoundingBox bounds = record.plan().geometry().bounds();
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && bounds.isInside(player.blockPosition())) {
                return true;
            }
        }
        return false;
    }

    static List<ServerPlayer> players(ServerLevel level, BoundingBox box, Predicate<ServerPlayer> filter) {
        List<ServerPlayer> players = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (box.isInside(player.blockPosition()) && filter.test(player)) {
                players.add(player);
            }
        }
        return players;
    }
}
