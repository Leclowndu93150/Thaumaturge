package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.config.labyrinth.LabyrinthConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.EldritchPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchStructure;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.PortalLink;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class AltarRituals {
    private static final int PARTICLE_INTERVAL = 4;
    private static final int PARTICLE_COUNT = 8;
    private static final double PARTICLE_SPREAD = 0.6;
    private static final double PARTICLE_SPEED = 0.05;
    private static final double PARTICLE_RISE = 1.5;
    private static final int CHANT_INTERVAL = 40;
    private static final float MIN_DRAIN_FRACTION = 0.5F;

    private AltarRituals() {}

    static void start(ServerLevel level, BlockEntityEldritchAltar altar, ServerPlayer player) {
        if (altar.ritual().isPresent()) {
            return;
        }
        BlockPos pos = altar.getBlockPos();
        if (altar.getEyes() < BlockEntityEldritchAltar.MAX_EYES) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.need_eyes");
            return;
        }
        if (!KnowledgeAccess.of(player).isResearchComplete(TTIds.RESEARCH_OCULUS)) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.ritual_unknown");
            return;
        }
        if (linkedMazeActive(level, altar)) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.already_open");
            return;
        }
        if (LabyrinthService.atCapacity(level.getServer())) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.labyrinth_full");
            return;
        }
        if (!level.getBlockState(pos.above()).is(TTBlocks.NODE.get()) && !altar.portalOpen()) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.no_node");
            return;
        }
        altar.setRitual(Optional.of(new AltarRitual(player.getUUID(), level.getGameTime(), 0.0F)));
        level.playSound(null, pos, TTSounds.CRAFTSTART.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        TTActionBar.sendPurple(player, "gui.thaumaturge.altar.ritual_begin");
    }

    static void tick(ServerLevel level, BlockEntityEldritchAltar altar, AltarRitual ritual) {
        LabyrinthConfig config = ThaumaturgeServerConfig.LABYRINTH;
        BlockPos pos = altar.getBlockPos();
        ServerPlayer caster = level.getServer().getPlayerList().getPlayer(ritual.caster());
        double reach = config.ritualMaxDistance.get();
        if (caster == null
                || !caster.isAlive()
                || caster.level() != level
                || caster.distanceToSqr(Vec3.atCenterOf(pos)) > reach * reach) {
            fizzle(level, altar, caster, "gui.thaumaturge.altar.ritual_broken");
            return;
        }
        int channel = config.ritualChannelTicks.get();
        float perTick = (float) (config.ritualVisCost.get() / channel);
        float drained = perTick > 0.0F ? AuraHelper.drainVis(level, pos, perTick, false) : 0.0F;
        if (perTick > 0.0F && drained < perTick * MIN_DRAIN_FRACTION) {
            fizzle(level, altar, caster, "gui.thaumaturge.altar.not_enough_vis");
            return;
        }
        altar.replaceRitual(ritual.drain(drained));
        long elapsed = level.getGameTime() - ritual.start();
        if (elapsed % PARTICLE_INTERVAL == 0) {
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    pos.getX() + 0.5,
                    pos.getY() + PARTICLE_RISE,
                    pos.getZ() + 0.5,
                    PARTICLE_COUNT,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    PARTICLE_SPEED);
        }
        if (elapsed % CHANT_INTERVAL == 0) {
            level.playSound(null, pos, TTSounds.CHANT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (elapsed >= channel) {
            complete(level, altar, caster);
        }
    }

    private static void complete(ServerLevel level, BlockEntityEldritchAltar altar, ServerPlayer caster) {
        BlockPos pos = altar.getBlockPos();
        Optional<ResourceKey<LabyrinthDefinition>> definition = AltarSite.definition(level, altar)
                .flatMap(site -> site.labyrinths().getRandomValue(level.getRandom()));
        Optional<MazeRecord> record = LabyrinthService.open(
                level.getServer(), GlobalPos.of(level.dimension(), pos), definition.map(ResourceKey::location));
        if (record.isEmpty()) {
            fizzle(level, altar, caster, "gui.thaumaturge.altar.labyrinth_full");
            return;
        }
        altar.setRitual(Optional.empty());
        altar.setEyes(0);
        altar.setLink(Optional.of(record.get().plan().id()));
        EldritchPlacement.place(
                        level,
                        pos.above(),
                        TTBlocks.ELDRITCH_PORTAL.get().defaultBlockState(),
                        TTBlockEntities.ELDRITCH_PORTAL.get())
                .ifPresent(portal -> portal.setLink(
                        PortalLink.intoLabyrinth(record.get().plan().id())));
        if (ThaumaturgeServerConfig.LABYRINTH.sealSiteWhileLinked.get()) {
            SiteSealing.apply(level, pos, true);
        }
        altar.setGarrison(altar.garrison().quell());
        level.playSound(null, pos, TTSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        TTActionBar.sendPurple(caster, "gui.thaumaturge.altar.ritual_complete");
    }

    static void updateSeal(ServerLevel level, BlockEntityEldritchAltar altar) {
        boolean sealed = ThaumaturgeServerConfig.LABYRINTH.sealSiteWhileLinked.get() && linkedMazeActive(level, altar);
        BlockState state = altar.getBlockState();
        if (state.hasProperty(BlockEldritchStructure.SEALED)
                && state.getValue(BlockEldritchStructure.SEALED) != sealed) {
            SiteSealing.apply(level, altar.getBlockPos(), sealed);
        }
    }

    private static void fizzle(
            ServerLevel level, BlockEntityEldritchAltar altar, @Nullable ServerPlayer caster, String message) {
        altar.setRitual(Optional.empty());
        level.playSound(null, altar.getBlockPos(), TTSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (caster != null) {
            TTActionBar.sendPurple(caster, message);
        }
    }

    private static boolean linkedMazeActive(ServerLevel level, BlockEntityEldritchAltar altar) {
        return altar.portalOpen()
                && altar.link()
                        .flatMap(id -> LabyrinthService.byId(level.getServer(), id))
                        .map(record -> record.state().phase())
                        .filter(phase -> phase != LabyrinthPhase.CONQUERED && phase != LabyrinthPhase.RETIRED)
                        .isPresent();
    }
}
