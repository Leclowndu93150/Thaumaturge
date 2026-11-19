package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.config.labyrinth.LabyrinthConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaceProvider.TrackedBlock;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTTicketTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public final class LabyrinthTransit {
    private static final long NOTICE_INTERVAL = 40;
    private static final int CREATIVE_WARMUP = 1;
    private static final String CLOSED = "gui.thaumaturge.labyrinth.closed";

    private LabyrinthTransit() {}

    public static boolean touch(ServerLevel level, ServerPlayer player, Optional<PortalLink> link) {
        return touch(level, player, link, ThaumaturgeServerConfig.LABYRINTH.enabled.get());
    }

    static boolean touch(ServerLevel level, ServerPlayer player, Optional<PortalLink> link, boolean enabled) {
        TransitState state = player.getData(TTAttachments.LABYRINTH_TRANSIT);
        long now = level.getGameTime();
        if (entryDisabled(enabled, link, level.dimension())) {
            release(level.getServer(), state);
            if (state.noticeDue(now, NOTICE_INTERVAL))
                TTActionBar.sendPurple(player, "gui.thaumaturge.labyrinth.disabled");
            return false;
        }
        if (now < state.cooldownUntil()) {
            return false;
        }
        Optional<Target> target = link.flatMap(value -> resolve(level.getServer(), value, false));
        if (target.isEmpty()) {
            if (state.noticeDue(now, NOTICE_INTERVAL)) {
                TTActionBar.sendPurple(player, CLOSED);
            }
            return false;
        }
        Optional<TransitState.Pending> pending = state.pending();
        ServerLevel destination = target.get().level();
        ChunkPos chunk = new ChunkPos(target.get().worldAnchor());
        int radius = ThaumaturgeServerConfig.LABYRINTH.preloadRadiusChunks.get();
        if (pending.isEmpty()
                || !pending.get().link().equals(link.get())
                || pending.get().level() != destination.dimension()
                || pending.get().chunk().getChessboardDistance(chunk)
                        > pending.get().radius()
                || now - pending.get().started() > TTTicketTypes.TRANSIT_LIFETIME) {
            release(level.getServer(), state);
            CompletableFuture<?> loaded = preload(destination, chunk, radius);
            state.setPending(Optional.of(
                    new TransitState.Pending(link.get(), destination.dimension(), chunk, radius, loaded, now)));
        }
        return true;
    }

    static boolean entryDisabled(boolean enabled, Optional<PortalLink> link, ResourceKey<Level> dimension) {
        return !enabled
                && link.map(value -> value.kind() == PortalLink.Kind.TO_LABYRINTH)
                        .orElse(!dimension.equals(OuterLands.DIMENSION));
    }

    private static CompletableFuture<Void> preload(ServerLevel level, ChunkPos center, int radius) {
        ServerChunkCache chunks = level.getChunkSource();
        chunks.addRegionTicket(TTTicketTypes.LABYRINTH_TRANSIT, center, radius, center);
        return CompletableFuture.supplyAsync(
                        () -> {
                            List<CompletableFuture<?>> pending = new ArrayList<>();
                            for (int x = center.x - radius; x <= center.x + radius; x++) {
                                for (int z = center.z - radius; z <= center.z + radius; z++) {
                                    pending.add(chunks.getChunkFuture(x, z, ChunkStatus.FULL, true));
                                }
                            }
                            return CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new));
                        },
                        Util.backgroundExecutor())
                .thenCompose(future -> future);
    }

    public static int transitionTime(ServerLevel level, ServerPlayer player) {
        Optional<TransitState.Pending> pending =
                player.getData(TTAttachments.LABYRINTH_TRANSIT).pending();
        if (pending.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        LabyrinthConfig config = ThaumaturgeServerConfig.LABYRINTH;
        boolean ready = pending.get().loaded().isDone()
                || level.getGameTime() - pending.get().started() >= config.maxPreloadWaitTicks.get();
        if (!ready) {
            return Integer.MAX_VALUE;
        }
        return player.getAbilities().invulnerable ? CREATIVE_WARMUP : config.warmupTicks.get();
    }

    public static Optional<DimensionTransition> destination(ServerLevel level, ServerPlayer player) {
        Optional<TransitState.Pending> pending =
                player.getData(TTAttachments.LABYRINTH_TRANSIT).pending();
        if (pending.isEmpty()) {
            return Optional.empty();
        }
        PortalLink link = pending.get().link();
        if (!ThaumaturgeServerConfig.LABYRINTH.enabled.get() && link.kind() == PortalLink.Kind.TO_LABYRINTH) {
            release(level.getServer(), player.getData(TTAttachments.LABYRINTH_TRANSIT));
            return Optional.empty();
        }
        Optional<Target> target = resolve(level.getServer(), link, true);
        if (target.isEmpty()) {
            return Optional.empty();
        }
        DimensionTransition.PostDimensionTransition post =
                DimensionTransition.PLAY_PORTAL_SOUND.then(LabyrinthTransit::finish);
        if (link.kind() == PortalLink.Kind.TO_LABYRINTH && target.get().maze().isPresent()) {
            ArrivalPad.Arrival arrival =
                    ArrivalPad.prepare(target.get().level(), target.get().maze().get());
            return Optional.of(
                    new DimensionTransition(target.get().level(), arrival.pos(), Vec3.ZERO, arrival.yaw(), 0.0F, post));
        }
        return Optional.of(ReturnPointFinder.find(
                player,
                target.get().level(),
                target.get().anchor(),
                target.get().worldAnchor(),
                post));
    }

    public static DimensionTransition returnTransition(
            ServerPlayer player, Optional<MazeRecord> record, DimensionTransition.PostDimensionTransition post) {
        Target target = origin(player.level().getServer(), record, true);
        return ReturnPointFinder.find(player, target.level(), target.anchor(), target.worldAnchor(), post);
    }

    public static void abandon(ServerPlayer player) {
        release(player.level().getServer(), player.getData(TTAttachments.LABYRINTH_TRANSIT));
    }

    private static void finish(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            TransitState state = player.getData(TTAttachments.LABYRINTH_TRANSIT);
            release(player.level().getServer(), state);
            state.setCooldownUntil(
                    player.level().getGameTime() + ThaumaturgeServerConfig.LABYRINTH.cooldownTicks.get());
        }
    }

    private static void release(MinecraftServer server, TransitState state) {
        state.pending().ifPresent(pending -> {
            ServerLevel level = server.getLevel(pending.level());
            if (level != null) {
                level.getChunkSource()
                        .removeRegionTicket(
                                TTTicketTypes.LABYRINTH_TRANSIT, pending.chunk(), pending.radius(), pending.chunk());
            }
        });
        state.setPending(Optional.empty());
    }

    private static Optional<Target> resolve(MinecraftServer server, PortalLink link, boolean loadSubLevel) {
        Optional<MazeRecord> record = LabyrinthService.byId(server, link.maze());
        return switch (link.kind()) {
            case TO_LABYRINTH ->
                record.filter(maze -> maze.state().phase() != LabyrinthPhase.RETIRED)
                        .flatMap(maze -> LabyrinthService.outer(server)
                                .flatMap(outer -> maze.plan()
                                        .landmarks()
                                        .point(LabyrinthLandmarks.ARRIVAL)
                                        .map(anchor -> new Target(outer, anchor, anchor, record))));
            case TO_ORIGIN -> Optional.of(origin(server, record, loadSubLevel));
        };
    }

    private static Target origin(MinecraftServer server, Optional<MazeRecord> record, boolean loadSubLevel) {
        Optional<GlobalPos> origin = record.map(maze -> maze.plan().origin());
        Optional<ServerLevel> level = origin.flatMap(pos -> Optional.ofNullable(server.getLevel(pos.dimension())));
        if (level.isPresent()) {
            ServerLevel originLevel = level.get();
            BlockPos stored = origin.get().pos();
            TrackedBlock anchor = LevelSpaces.trackedBlock(
                            originLevel,
                            LabyrinthService.originTrackingId(
                                    record.orElseThrow().plan().id()),
                            loadSubLevel)
                    .orElseGet(() -> new TrackedBlock(stored, LevelSpaces.worldBlock(originLevel, stored)));
            return new Target(originLevel, anchor.position(), anchor.worldPosition(), record);
        }
        GlobalPos spawn =
                GlobalPos.of(server.overworld().dimension(), server.overworld().getSharedSpawnPos());
        ServerLevel spawnLevel =
                Optional.ofNullable(server.getLevel(spawn.dimension())).orElseGet(server::overworld);
        return new Target(spawnLevel, spawn.pos(), spawn.pos(), record);
    }

    private record Target(ServerLevel level, BlockPos anchor, BlockPos worldAnchor, Optional<MazeRecord> maze) {}
}
