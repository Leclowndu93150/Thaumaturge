package com.leclowndu93150.thaumaturge.content.eldritch.reliquary;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.guardian.GuardianPosts;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.network.ClientboundReliquaryViewPayload;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

final class ReliquaryViews {
    private static final double VIEW_RANGE = 24.0;

    private ReliquaryViews() {}

    static Optional<MazeRecord> record(ServerLevel level, BlockEntityEldritchReliquary reliquary) {
        return LabyrinthService.resolve(level, reliquary.maze(), reliquary.getBlockPos());
    }

    static ReliquaryView view(MazeRecord record, BlockEntityEldritchReliquary reliquary, UUID player) {
        if (record.state().claims().claimed(reliquary.claimKey(), player)) {
            return ReliquaryView.CLAIMED;
        }
        return switch (reliquary.role()) {
            case KEY_ROOM ->
                ThaumaturgeServerConfig.LABYRINTH.keyRoomRequiresWard.get()
                                && !GuardianPosts.wardOpen(record, GuardianPosts.KEY_ROOM_WARD)
                        ? ReliquaryView.WARDED
                        : ReliquaryView.CLAIMABLE;
            case CACHE -> ReliquaryView.CLAIMABLE;
            case BOSS ->
                record.state().phase() == LabyrinthPhase.CONQUERED
                                && record.state().encounter().participation().eligible(player)
                        ? ReliquaryView.CLAIMABLE
                        : ReliquaryView.INELIGIBLE;
        };
    }

    static void push(ServerLevel level, BlockEntityEldritchReliquary reliquary) {
        Optional<MazeRecord> record = record(level, reliquary);
        if (record.isEmpty()) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(reliquary.getBlockPos());
        Set<UUID> inRange = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center) <= VIEW_RANGE * VIEW_RANGE) {
                inRange.add(player.getUUID());
                send(reliquary, record.get(), player, false);
            }
        }
        reliquary.sentViews().keySet().retainAll(inRange);
    }

    static void send(BlockEntityEldritchReliquary reliquary, MazeRecord record, ServerPlayer player, boolean force) {
        ReliquaryView view = view(record, reliquary, player.getUUID());
        if (!force && reliquary.sentViews().get(player.getUUID()) == view) {
            return;
        }
        reliquary.sentViews().put(player.getUUID(), view);
        PacketDistributor.sendToPlayer(player, new ClientboundReliquaryViewPayload(reliquary.getBlockPos(), view));
    }
}
