package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.config.labyrinth.AnnounceScope;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class LabyrinthAnnouncer {
    private static final int FADE_IN = 10;
    private static final int STAY = 70;
    private static final int FADE_OUT = 20;

    private LabyrinthAnnouncer() {}

    public static void title(ServerLevel level, MazeRecord record, Component title, Component subtitle) {
        for (ServerPlayer player : audience(level, record)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(FADE_IN, STAY, FADE_OUT));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }

    public static void actionBar(ServerLevel level, MazeRecord record, Component message) {
        for (ServerPlayer player : audience(level, record)) {
            TTActionBar.send(player, message);
        }
    }

    private static List<ServerPlayer> audience(ServerLevel level, MazeRecord record) {
        AnnounceScope scope = ThaumaturgeServerConfig.LABYRINTH.announceScope.get();
        BlockPos anchor = EncounterGeometry.anchor(record);
        return switch (scope) {
            case MAZE ->
                EncounterGeometry.players(level, record.plan().geometry().bounds(), player -> true);
            case HALL -> EncounterGeometry.players(level, EncounterGeometry.hall(record), player -> true);
            case NEARBY -> {
                double range = ThaumaturgeServerConfig.LABYRINTH.announceNearbyRange.get();
                List<ServerPlayer> nearby = new ArrayList<>();
                for (ServerPlayer player : level.players()) {
                    if (player.blockPosition().closerThan(anchor, range)) {
                        nearby.add(player);
                    }
                }
                yield nearby;
            }
        };
    }
}
