package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterScaling;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterSettings;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

final class EncounterMonitor {
    private EncounterMonitor() {}

    static void tick(ServerLevel level, MazeRecord record, EncounterSessions sessions, long time) {
        EncounterState state = record.state().encounter();
        Optional<Holder.Reference<LabyrinthEncounter>> active = EncounterResolver.active(level, record);
        Optional<EncounterSettings> settings =
                active.map(holder -> holder.value().settings());
        List<ServerPlayer> hall = EncounterGeometry.hallPlayers(level, record);
        for (ServerPlayer player : hall) {
            state.participation().addPresence(player.getUUID(), LabyrinthDirector.RUN_INTERVAL);
        }
        active.ifPresent(holder -> holder.value()
                .tick(new MazeEncounterContext(
                        level, record, holder.value().settings(), Math.max(1, state.scaledFor()))));
        EncounterScaling scaling = settings.map(EncounterSettings::scaling).orElse(EncounterScaling.DEFAULT);
        boolean changed = track(level, record, state, time, scaling);
        changed |= rescale(level, state, hall.size(), scaling);
        changed |= wipe(level, state, hall.isEmpty(), time);
        updateBar(level, record, sessions, state, hall, settings);
        if (state.primariesDefeated()) {
            int delay = settings.map(EncounterSettings::victoryDelay).orElse(EncounterSettings.DEFAULT_VICTORY_DELAY);
            if (state.victoryAt() < 0) {
                state.setVictoryAt(time);
                changed = true;
            } else if (time - state.victoryAt() >= delay) {
                EncounterPhases.conquer(level, record);
                return;
            }
        }
        if (changed) {
            EncounterPhases.dirty(level);
        }
    }

    private static boolean track(
            ServerLevel level, MazeRecord record, EncounterState state, long time, EncounterScaling scaling) {
        BlockPos anchor = EncounterGeometry.anchor(record);
        if (level.getDifficulty() == Difficulty.PEACEFUL
                || !LabyrinthService.entitiesLoaded(
                        level, anchor, ThaumaturgeServerConfig.LABYRINTH.bossLeashRadius.get())) {
            return false;
        }
        long respawnAfter = ThaumaturgeServerConfig.LABYRINTH.missingBossRespawnTicks.get();
        boolean changed = false;
        boolean respawned = false;
        for (int i = state.bound().size() - 1; i >= 0; i--) {
            BoundEntity bound = state.bound().get(i);
            if (!bound.primary() || bound.defeated()) {
                continue;
            }
            Entity entity = level.getEntity(bound.uuid());
            if (entity != null) {
                if (entity.isAlive() && bound.missingSince() >= 0) {
                    state.set(i, bound.present());
                    changed = true;
                }
                continue;
            }
            if (bound.missingSince() < 0) {
                state.set(i, bound.missingFrom(time));
                changed = true;
            } else if (time - bound.missingSince() >= respawnAfter
                    && EncounterSpawner.spawn(
                                    level,
                                    record,
                                    bound.type(),
                                    anchor,
                                    EncounterRole.PRIMARY,
                                    scaling,
                                    Math.max(1, state.scaledFor()))
                            .isPresent()) {
                state.remove(i);
                respawned = true;
                changed = true;
            }
        }
        if (respawned && state.primaryCount() > 1) {
            EncounterPhases.shareBar(level, state);
        }
        return changed;
    }

    private static boolean rescale(ServerLevel level, EncounterState state, int present, EncounterScaling scaling) {
        if (present <= state.scaledFor()) {
            return false;
        }
        for (LivingEntity living : living(level, state, false)) {
            EncounterSpawner.scale(living, scaling, present, false);
        }
        state.setScaledFor(present);
        return true;
    }

    private static boolean wipe(ServerLevel level, EncounterState state, boolean hallEmpty, long time) {
        int resetAfter = ThaumaturgeServerConfig.LABYRINTH.wipeResetTicks.get();
        if (!hallEmpty) {
            if (state.emptySince() >= 0) {
                state.setEmptySince(-1L);
                return true;
            }
            return false;
        }
        if (state.emptySince() < 0) {
            state.setEmptySince(time);
            return true;
        }
        if (resetAfter < 0 || time - state.emptySince() < resetAfter) {
            return false;
        }
        for (LivingEntity living : living(level, state, false)) {
            living.setHealth(living.getMaxHealth());
        }
        state.setEmptySince(time);
        return true;
    }

    private static void updateBar(
            ServerLevel level,
            MazeRecord record,
            EncounterSessions sessions,
            EncounterState state,
            List<ServerPlayer> hall,
            Optional<EncounterSettings> settings) {
        int id = record.plan().id().value();
        int primaries = state.primaryCount();
        if (primaries <= 1) {
            sessions.endBar(id);
            return;
        }
        ServerBossEvent bar = sessions.bar(
                id,
                () -> new ServerBossEvent(
                        settings.map(EncounterSettings::name)
                                .orElse(Component.translatable("gui.thaumaturge.labyrinth.encounter")),
                        settings.map(EncounterSettings::barColor).orElse(BossEvent.BossBarColor.PURPLE),
                        BossEvent.BossBarOverlay.PROGRESS));
        float health = 0.0F;
        for (BoundEntity bound : state.bound()) {
            if (!bound.primary() || bound.defeated()) {
                continue;
            }
            Entity entity = level.getEntity(bound.uuid());
            health += entity instanceof LivingEntity living && living.getMaxHealth() > 0.0F
                    ? living.getHealth() / living.getMaxHealth()
                    : 1.0F;
        }
        bar.setProgress(Mth.clamp(health / primaries, 0.0F, 1.0F));
        for (ServerPlayer player : List.copyOf(bar.getPlayers())) {
            if (!hall.contains(player)) {
                bar.removePlayer(player);
            }
        }
        for (ServerPlayer player : hall) {
            bar.addPlayer(player);
        }
    }

    static List<LivingEntity> living(ServerLevel level, EncounterState state, boolean primaryOnly) {
        List<LivingEntity> living = new ArrayList<>();
        for (BoundEntity bound : state.bound()) {
            if ((bound.primary() || !primaryOnly)
                    && !bound.defeated()
                    && level.getEntity(bound.uuid()) instanceof LivingEntity entity
                    && entity.isAlive()) {
                living.add(entity);
            }
        }
        return living;
    }
}
