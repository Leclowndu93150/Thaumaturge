package com.leclowndu93150.thaumaturge.content.eldritch.wayfinding;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeView;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthGameplay;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;

public final class TabletResonance {
    private static final float VOLUME = 0.5F;
    private static final float FAR_PITCH = 0.6F;
    private static final float NEAR_PITCH = 1.6F;
    private static final float WARM_BAND = 0.66F;
    private static final float FIERCE_BAND = 0.33F;

    private TabletResonance() {}

    public static void tick(ServerLevel level, ServerPlayer player, MazeId maze, EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND
                || level.dimension() != OuterLands.DIMENSION
                || !ThaumaturgeServerConfig.LABYRINTH.tabletResonance.get()) {
            return;
        }
        Optional<MazeRecord> record = LabyrinthService.find(level, player.blockPosition());
        if (record.isEmpty()
                || !record.get().plan().id().equals(maze)
                || record.get().state().phase() != LabyrinthPhase.SEALED
                || !WayfindingDirector.gameplay(level, record.get())
                        .map(LabyrinthGameplay::tabletResonance)
                        .orElse(true)) {
            return;
        }
        Optional<Integer> cells =
                new MazeView(record.get()).cellsToward(LabyrinthLandmarks.BOSS_DOOR, player.blockPosition());
        if (cells.isEmpty()) {
            return;
        }
        float distance = Mth.clamp(
                cells.get() / (float) ThaumaturgeServerConfig.LABYRINTH.tabletResonanceRangeCells.get(), 0.0F, 1.0F);
        int min = ThaumaturgeServerConfig.LABYRINTH.tabletResonanceIntervalMin.get();
        int max = Math.max(min, ThaumaturgeServerConfig.LABYRINTH.tabletResonanceIntervalMax.get());
        int interval = Math.max(1, Math.round(Mth.lerp(distance, min, max)));
        if (level.getGameTime() % interval != 0) {
            return;
        }
        PrivateCues.chime(
                player,
                player.position(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                VOLUME,
                Mth.lerp(distance, NEAR_PITCH, FAR_PITCH));
        TTActionBar.sendPurple(
                player,
                distance > WARM_BAND
                        ? "gui.thaumaturge.runed_tablet.faint"
                        : distance > FIERCE_BAND
                                ? "gui.thaumaturge.runed_tablet.warm"
                                : "gui.thaumaturge.runed_tablet.fierce");
    }
}
