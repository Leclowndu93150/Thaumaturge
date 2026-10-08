package com.leclowndu93150.thaumaturge.content.eldritch.wayfinding;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeView;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.LabyrinthKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

enum WayfindingObjective {
    KEY(0xFFE8B84A, 1.2F),
    LOCK(0xFFA070E0, 1.0F),
    HALL(0xFFD04040, 0.8F),
    EXIT(0xFF60D0E0, 1.4F);

    private final int color;
    private final float pitch;

    WayfindingObjective(int color, float pitch) {
        this.color = color;
        this.pitch = pitch;
    }

    int color() {
        return color;
    }

    float pitch() {
        return pitch;
    }

    static WayfindingObjective of(MazeRecord record, ServerPlayer player) {
        LabyrinthPhase phase = record.state().phase();
        if (phase == LabyrinthPhase.SEALED) {
            return LabyrinthKeys.carries(player, record.plan().id()) ? LOCK : KEY;
        }
        return phase.isContested() ? HALL : EXIT;
    }

    ResourceLocation target(MazeView view, BlockPos from) {
        return switch (this) {
            case KEY -> LabyrinthLandmarks.KEY;
            case LOCK, HALL -> LabyrinthLandmarks.BOSS_DOOR;
            case EXIT -> nearerExit(view, from);
        };
    }

    private static ResourceLocation nearerExit(MazeView view, BlockPos from) {
        int toRift = view.cellsToward(LabyrinthLandmarks.BOSS_DOOR, from).orElse(Integer.MAX_VALUE);
        int toPortal = view.cellsToward(LabyrinthLandmarks.ARRIVAL, from).orElse(Integer.MAX_VALUE);
        return toRift < toPortal ? LabyrinthLandmarks.BOSS_DOOR : LabyrinthLandmarks.ARRIVAL;
    }
}
