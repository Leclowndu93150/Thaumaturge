package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import java.util.Optional;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class MazeRegeneration {
    private static final int CHECK_INTERVAL = 100;

    private MazeRegeneration() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != OuterLands.DIMENSION
                || level.getGameTime() % CHECK_INTERVAL != 0) {
            return;
        }
        int delay = ThaumaturgeServerConfig.LABYRINTH.regenerateConqueredAfterTicks.get();
        if (delay < 0 || LabyrinthService.runtime(level).isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        LabyrinthData data = LabyrinthData.get(level);
        for (MazeRecord record : data.records()) {
            MazeState state = record.state();
            if (state.phase() != LabyrinthPhase.CONQUERED) {
                continue;
            }
            if (occupied(level, record.plan())) {
                if (state.visit(now)) {
                    data.setDirty();
                }
                continue;
            }
            if (now - Math.max(state.lastVisited(), state.phaseSince()) >= delay) {
                regenerate(level, record, now);
                return;
            }
        }
    }

    private static boolean occupied(ServerLevel level, MazePlan plan) {
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator()
                    && plan.geometry()
                            .containsChunk(
                                    SectionPos.blockToSectionCoord(player.getBlockX()),
                                    SectionPos.blockToSectionCoord(player.getBlockZ()),
                                    MazeRegions.MARGIN_CHUNKS)) {
                return true;
            }
        }
        return false;
    }

    private static void regenerate(ServerLevel level, MazeRecord record, long now) {
        int id = record.plan().id().value();
        Optional<MazeRecord> fresh = LabyrinthService.regenerate(level, record);
        if (fresh.isPresent()) {
            Thaumaturge.LOGGER.info(
                    "Regenerated labyrinth #{} with a new layout, generation {}",
                    id,
                    fresh.get().plan().generation());
            return;
        }
        record.state().setPhase(LabyrinthPhase.RETIRED, now);
        LabyrinthData.get(level).setDirty();
        Thaumaturge.LOGGER.error("Labyrinth #{} could not be regenerated and was retired instead", id);
    }
}
