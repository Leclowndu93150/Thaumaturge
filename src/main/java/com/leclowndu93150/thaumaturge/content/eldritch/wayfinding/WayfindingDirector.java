package com.leclowndu93150.thaumaturge.content.eldritch.wayfinding;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeView;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthGameplay;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class WayfindingDirector {
    private static final int RUN_INTERVAL = 10;
    private static final int MOTES = 10;
    private static final double MOTE_STEP = 0.3;
    private static final double MOTE_SPEED = 0.06;
    private static final double FACE_OFFSET = 0.6;
    private static final int MOTE_AGE = 40;
    private static final float CHIME_VOLUME = 0.35F;

    private WayfindingDirector() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != OuterLands.DIMENSION
                || level.getGameTime() % RUN_INTERVAL != 0
                || !ThaumaturgeServerConfig.LABYRINTH.glyphHints.get()) {
            return;
        }
        long now = level.getGameTime();
        int cooldown = ThaumaturgeServerConfig.LABYRINTH.glyphPulseCooldownTicks.get();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || now - player.getData(TTAttachments.WAYFINDING_PULSE) < cooldown) {
                continue;
            }
            Optional<MazeRecord> record = LabyrinthService.find(level, player.blockPosition());
            if (record.isEmpty()
                    || !gameplay(level, record.get())
                            .map(LabyrinthGameplay::glyphHints)
                            .orElse(true)) {
                continue;
            }
            nearestGlyph(record.get(), player).ifPresent(glyph -> {
                if (pulse(player, record.get(), glyph)) {
                    player.setData(TTAttachments.WAYFINDING_PULSE, now);
                }
            });
        }
    }

    static Optional<LabyrinthGameplay> gameplay(ServerLevel level, MazeRecord record) {
        return LabyrinthService.definition(level.getServer(), record).map(LabyrinthDefinition::gameplay);
    }

    private static Optional<BlockPos> nearestGlyph(MazeRecord record, ServerPlayer player) {
        BlockPos best = null;
        double range = ThaumaturgeServerConfig.LABYRINTH.glyphRange.get();
        double bestDistance = range * range;
        for (BlockPos glyph : record.plan().landmarks().glyphs()) {
            double distance = glyph.distToCenterSqr(player.position());
            if (distance <= bestDistance) {
                best = glyph;
                bestDistance = distance;
            }
        }
        return Optional.ofNullable(best);
    }

    private static boolean pulse(ServerPlayer player, MazeRecord record, BlockPos glyph) {
        MazeView view = new MazeView(record);
        WayfindingObjective objective = WayfindingObjective.of(record, player);
        Optional<Direction> step =
                view.directionToward(objective.target(view, player.blockPosition()), player.blockPosition());
        if (step.isEmpty()) {
            return false;
        }
        Vec3 center = Vec3.atCenterOf(glyph);
        Vec3 face =
                center.add(player.getEyePosition().subtract(center).normalize().scale(FACE_OFFSET));
        Vec3 along = Vec3.atLowerCornerOf(step.get().getNormal());
        WispyMoteParticleOptions mote = new WispyMoteParticleOptions(
                objective.color(), MOTE_AGE, 0.0F, WispyMoteParticleOptions.NO_ENTITY, true);
        for (int i = 0; i < MOTES; i++) {
            PrivateCues.particle(player, mote, face.add(along.scale(i * MOTE_STEP)), along.scale(MOTE_SPEED));
        }
        PrivateCues.chime(player, face, SoundEvents.AMETHYST_BLOCK_CHIME, CHIME_VOLUME, objective.pitch());
        return true;
    }
}
