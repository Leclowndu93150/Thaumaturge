package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.guardian.GuardianPosts;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthData;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthRuntime;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.registry.TTTicketTypes;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LabyrinthDirector {
    static final int RUN_INTERVAL = 20;
    private static final int HALL_TICKET_RADIUS = 2;

    private LabyrinthDirector() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != OuterLands.DIMENSION) {
            return;
        }
        Optional<LabyrinthRuntime> runtime = LabyrinthService.runtime(level);
        if (runtime.isEmpty()) {
            return;
        }
        EncounterSessions sessions = runtime.get().encounters();
        sessions.tickJobs(level, ThaumaturgeServerConfig.LABYRINTH.manifestBlocksPerTick.get());
        long time = level.getGameTime();
        for (MazeRecord record : List.copyOf(LabyrinthData.get(level).records())) {
            int id = record.plan().id().value();
            if (Math.floorMod(time + id, RUN_INTERVAL) != 0) {
                continue;
            }
            switch (record.state().phase()) {
                case CHARGING -> charging(level, record, sessions, time);
                case ACTIVE -> EncounterMonitor.tick(level, record, sessions, time);
                default -> {}
            }
            if (EncounterGeometry.occupied(level, record)) {
                GuardianPosts.refresh(level, record);
                if (Math.floorMod(
                                time / RUN_INTERVAL + id,
                                Math.max(
                                        1,
                                        ThaumaturgeServerConfig.LABYRINTH.integrityIntervalTicks.get() / RUN_INTERVAL))
                        == 0) {
                    LabyrinthIntegrity.check(level, record);
                }
            }
        }
    }

    private static void charging(ServerLevel level, MazeRecord record, EncounterSessions sessions, long time) {
        ChunkPos chunk = new ChunkPos(EncounterGeometry.anchor(record));
        level.getChunkSource().addRegionTicket(TTTicketTypes.LABYRINTH_ENCOUNTER, chunk, HALL_TICKET_RADIUS, chunk);
        EncounterPhases.ensureArena(level, record, sessions);
        boolean manifested = sessions.job(record.plan().id().value())
                .map(ArenaManifestJob::done)
                .orElse(true);
        if (manifested
                && time - record.state().phaseSince() >= ThaumaturgeServerConfig.LABYRINTH.lockChargeTicks.get()) {
            EncounterPhases.activate(level, record);
        }
    }
}
