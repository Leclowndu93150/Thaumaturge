package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world.RoomStamper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkTicketLevelUpdatedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LabyrinthTicker {
    private static final int SWEEP_INTERVAL = 6000;

    private LabyrinthTicker() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != OuterLands.DIMENSION) {
            return;
        }
        ChunkAccess chunk = event.getChunk();
        ChunkPos pos = chunk.getPos();
        LabyrinthService.runtime(level).ifPresent(runtime -> {
            MazePlan plan = runtime.index().planAt(pos.x, pos.z, 0);
            if (plan != null && !RoomStamper.isStamped(chunk, plan)) {
                runtime.queueRepair(pos.x, pos.z);
            }
        });
    }

    @SubscribeEvent
    public static void onChunkLevelChange(ChunkTicketLevelUpdatedEvent event) {
        ServerLevel level = event.getLevel();
        if (level.dimension() != OuterLands.DIMENSION
                || ChunkLevel.fullStatus(event.getOldTicketLevel()).isOrAfter(FullChunkStatus.FULL)
                || !ChunkLevel.fullStatus(event.getNewTicketLevel()).isOrAfter(FullChunkStatus.FULL)) {
            return;
        }
        ChunkPos pos = new ChunkPos(event.getChunkPos());
        LabyrinthService.runtime(level).ifPresent(runtime -> {
            if (runtime.index().planAt(pos.x, pos.z, 0) != null) {
                runtime.queueRepair(pos.x, pos.z);
            }
        });
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != OuterLands.DIMENSION) {
            return;
        }
        Optional<LabyrinthRuntime> runtime = LabyrinthService.runtime(level);
        if (runtime.isEmpty()) {
            return;
        }
        repair(level, runtime.get());
        if (level.getGameTime() % ThaumaturgeServerConfig.LABYRINTH.triggerIntervalTicks.get() == 0) {
            fireTriggers(level);
        }
        if (level.getGameTime() % SWEEP_INTERVAL == 0) {
            sweep(level);
        }
    }

    private static void sweep(ServerLevel level) {
        long now = level.getGameTime();
        Set<MazeId> occupied = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            LabyrinthService.find(level, player.blockPosition())
                    .ifPresent(record -> occupied.add(record.plan().id()));
        }
        List<MazeId> expired = new ArrayList<>();
        for (MazeRecord record : LabyrinthData.get(level).records()) {
            if (!occupied.contains(record.plan().id()) && expired(record.state(), now)) {
                expired.add(record.plan().id());
            }
        }
        for (MazeId id : expired) {
            LabyrinthService.retire(level, id);
            Thaumaturge.LOGGER.info(
                    "Retired labyrinth #{}: nobody has been inside it for the configured time", id.value());
        }
    }

    private static boolean expired(MazeState state, long now) {
        boolean conquered = state.phase() == LabyrinthPhase.CONQUERED || state.phase() == LabyrinthPhase.RETIRED;
        int days = conquered
                ? ThaumaturgeServerConfig.LABYRINTH.retireConqueredAfterDays.get()
                : ThaumaturgeServerConfig.LABYRINTH.retireAbandonedAfterDays.get();
        if (days < 0) {
            return false;
        }
        long since = Math.max(state.lastVisited(), conquered ? state.phaseSince() : state.createdAt());
        return now - since >= (long) days * SharedConstants.TICKS_PER_GAME_DAY;
    }

    private static void repair(ServerLevel level, LabyrinthRuntime runtime) {
        int budget = ThaumaturgeServerConfig.LABYRINTH.repairChunksPerTick.get();
        List<Long> notReady = new ArrayList<>();
        for (int i = 0; i < budget; i++) {
            Long key = runtime.nextRepair();
            if (key == null) {
                break;
            }
            ChunkPos pos = new ChunkPos(key);
            MazePlan plan = runtime.index().planAt(pos.x, pos.z, 0);
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (plan != null && chunk == null) {
                notReady.add(key);
                continue;
            }
            if (plan != null) {
                RoomStamper.ensureStamped(level, chunk, plan, runtime);
            }
            runtime.repairDone(key);
        }
        notReady.forEach(runtime::retryRepair);
    }

    private static void fireTriggers(ServerLevel level) {
        LabyrinthData data = LabyrinthData.get(level);
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            Optional<MazeRecord> record = LabyrinthService.find(level, player.blockPosition());
            if (record.isEmpty()) {
                continue;
            }
            MazeState state = record.get().state();
            if (state.visit(level.getGameTime())) {
                data.setDirty();
            }
            Iterator<PendingTrigger> pending = state.triggers().iterator();
            while (pending.hasNext()) {
                PendingTrigger trigger = pending.next();
                double radius = trigger.radius();
                if (player.blockPosition().distSqr(trigger.pos()) > radius * radius || !level.isLoaded(trigger.pos())) {
                    continue;
                }
                TriggerContext context =
                        new TriggerContext(level, record.get().plan().id(), trigger.index(), trigger.transform());
                if (!trigger.marker().canTrigger(context)) {
                    continue;
                }
                pending.remove();
                data.setDirty();
                trigger.marker().trigger(context, trigger.pos());
            }
        }
    }
}
