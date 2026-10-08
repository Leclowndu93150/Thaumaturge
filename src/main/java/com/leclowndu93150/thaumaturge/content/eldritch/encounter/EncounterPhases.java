package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterSettings;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.EldritchPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthData;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthRuntime;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEntityEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.PortalLink;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryPlacement;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class EncounterPhases {
    private static final int BARRIER_PARTICLES = 3;
    private static final double BARRIER_SPREAD = 0.3;
    private static final double BARRIER_PARTICLE_SPEED = 0.05;
    private static final float LOCK_VOLUME = 1.0F;
    private static final float DOOR_VOLUME = 1.5F;

    private EncounterPhases() {}

    public static void charge(ServerLevel level, MazeRecord record) {
        long now = level.getGameTime();
        EncounterState encounter = record.state().encounter();
        record.state().setPhase(LabyrinthPhase.CHARGING, now);
        encounter.charge(now);
        EncounterResolver.forActivation(level, record).ifPresent(holder -> encounter.choose(holder.key()));
        lock(level, record).ifPresent(lock -> lock.showCharge(now));
        record.plan()
                .landmarks()
                .point(LabyrinthLandmarks.BOSS_DOOR)
                .ifPresent(
                        pos -> level.playSound(null, pos, TTSounds.PUMP.get(), SoundSource.BLOCKS, LOCK_VOLUME, 1.0F));
        LabyrinthAnnouncer.title(
                level,
                record,
                Component.translatable("gui.thaumaturge.labyrinth.lock.title"),
                Component.translatable("gui.thaumaturge.labyrinth.lock.subtitle"));
        dirty(level);
    }

    static void ensureArena(ServerLevel level, MazeRecord record, EncounterSessions sessions) {
        int id = record.plan().id().value();
        if (sessions.hasJob(id)) {
            return;
        }
        Optional<ArenaManifestJob> job = EncounterResolver.active(level, record)
                .flatMap(holder -> holder.value().settings().arena())
                .flatMap(template -> ArenaManifestJob.create(level, template, EncounterGeometry.anchor(record)));
        sessions.startJob(id, job.orElseGet(ArenaManifestJob::none));
    }

    public static void activate(ServerLevel level, MazeRecord record) {
        int id = record.plan().id().value();
        sessions(level).ifPresent(sessions -> sessions.endJob(id));
        dissolveBarriers(level, record);
        record.state().setPhase(LabyrinthPhase.ACTIVE, level.getGameTime());
        Optional<Holder.Reference<LabyrinthEncounter>> encounter =
                EncounterResolver.active(level, record).or(() -> EncounterResolver.forActivation(level, record));
        if (encounter.isEmpty()) {
            Thaumaturge.LOGGER.error("Labyrinth {} has no encounter it can run; concluding it without a fight", id);
            conquer(level, record);
            return;
        }
        int participants =
                Math.max(1, EncounterGeometry.hallPlayers(level, record).size());
        EncounterState state = record.state().encounter();
        state.start(encounter.get().key(), participants);
        EncounterSettings settings = encounter.get().value().settings();
        encounter.get().value().begin(new MazeEncounterContext(level, record, settings, participants));
        if (state.primaryCount() == 0) {
            Thaumaturge.LOGGER.error(
                    "Labyrinth {} encounter {} spawned no boss; concluding it without a fight",
                    id,
                    encounter.get().key().location());
            conquer(level, record);
            return;
        }
        if (state.primaryCount() > 1) {
            shareBar(level, state);
        }
        state.participation().setPool(primaryHealth(level, state));
        LabyrinthAnnouncer.title(level, record, settings.name(), settings.announcement());
        dirty(level);
    }

    public static void conquer(ServerLevel level, MazeRecord record) {
        record.state().setPhase(LabyrinthPhase.CONQUERED, level.getGameTime());
        sessions(level).ifPresent(sessions -> sessions.endBar(record.plan().id().value()));
        record.state()
                .encounter()
                .participation()
                .freeze(
                        ThaumaturgeServerConfig.LABYRINTH.participationMinPresenceTicks.get(),
                        ThaumaturgeServerConfig.LABYRINTH
                                .participationMinDamageFraction
                                .get()
                                .floatValue(),
                        EncounterGeometry.mazePlayers(level, record));
        openSpoils(level, record);
        LabyrinthAnnouncer.title(
                level,
                record,
                Component.translatable("gui.thaumaturge.labyrinth.conquered.title"),
                Component.translatable("gui.thaumaturge.labyrinth.conquered.subtitle"));
        dirty(level);
    }

    public static void reset(ServerLevel level, MazeRecord record) {
        EncounterState state = record.state().encounter();
        for (BoundEntity bound : state.bound()) {
            Entity entity = level.getEntity(bound.uuid());
            if (entity != null) {
                entity.discard();
            }
        }
        state.reset();
        record.state().setPhase(LabyrinthPhase.SEALED, level.getGameTime());
        sessions(level).ifPresent(sessions -> sessions.forget(record.plan().id().value()));
        lock(level, record).ifPresent(BlockEntityEldritchLock::showSealed);
        LabyrinthService.requeue(level, record.plan(), record.plan().landmarks().bossHall());
        dirty(level);
    }

    static void openSpoils(ServerLevel level, MazeRecord record) {
        if (ThaumaturgeServerConfig.LABYRINTH.openExitRift.get()) {
            record.plan()
                    .landmarks()
                    .point(LabyrinthLandmarks.EXIT)
                    .ifPresent(pos -> restoreOriginPortal(level, record, pos));
        }
        ReliquaryPlacement.ensureBoss(level, record);
    }

    static void restoreOriginPortal(ServerLevel level, MazeRecord record, BlockPos pos) {
        EldritchPlacement.restore(
                        level,
                        pos,
                        TTBlocks.ELDRITCH_PORTAL.get(),
                        TTBlocks.ELDRITCH_PORTAL.get()::defaultBlockState,
                        TTBlockEntities.ELDRITCH_PORTAL.get())
                .ifPresent(portal ->
                        portal.setLink(PortalLink.toOrigin(record.plan().id())));
    }

    private static float primaryHealth(ServerLevel level, EncounterState state) {
        float total = 0.0F;
        for (LivingEntity living : EncounterMonitor.living(level, state, true)) {
            total += living.getMaxHealth();
        }
        return total;
    }

    static void dissolveBarriers(ServerLevel level, MazeRecord record) {
        boolean opened = false;
        for (BlockPos pos : record.plan().landmarks().barriers()) {
            if (!level.isLoaded(pos) || !level.getBlockState(pos).is(TTBlockTags.LABYRINTH_BARRIER)) {
                continue;
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    BARRIER_PARTICLES,
                    BARRIER_SPREAD,
                    BARRIER_SPREAD,
                    BARRIER_SPREAD,
                    BARRIER_PARTICLE_SPEED);
            opened = true;
        }
        if (opened) {
            record.plan()
                    .landmarks()
                    .point(LabyrinthLandmarks.BOSS_DOOR)
                    .ifPresent(pos -> level.playSound(
                            null, pos, TTSounds.EVILPORTAL.get(), SoundSource.BLOCKS, DOOR_VOLUME, 1.0F));
        }
    }

    static void shareBar(ServerLevel level, EncounterState state) {
        for (LivingEntity living : EncounterMonitor.living(level, state, true)) {
            LabyrinthBinding.on(living)
                    .ifPresent(binding -> living.setData(TTAttachments.LABYRINTH_BINDING, binding.withSharedBar()));
        }
    }

    static Optional<BlockEntityEldritchLock> lock(ServerLevel level, MazeRecord record) {
        return record.plan()
                .landmarks()
                .point(LabyrinthLandmarks.BOSS_DOOR)
                .filter(level::isLoaded)
                .flatMap(pos -> level.getBlockEntity(pos, TTBlockEntities.ELDRITCH_LOCK.get()));
    }

    static Optional<EncounterSessions> sessions(ServerLevel level) {
        return LabyrinthService.runtime(level).map(LabyrinthRuntime::encounters);
    }

    static void dirty(ServerLevel level) {
        LabyrinthData.get(level).setDirty();
    }
}
