package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.content.eldritch.EldritchPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryPlacement;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

final class LabyrinthIntegrity {
    private LabyrinthIntegrity() {}

    static void check(ServerLevel level, MazeRecord record) {
        record.plan()
                .landmarks()
                .point(LabyrinthLandmarks.ENTRY_PORTAL)
                .ifPresent(pos -> EncounterPhases.restoreOriginPortal(level, record, pos));
        record.plan().landmarks().point(LabyrinthLandmarks.BOSS_DOOR).ifPresent(pos -> lock(level, record, pos));
        ReliquaryPlacement.ensureKeyRoom(level, record);
        if (record.state().phase().isPastLock()) {
            EncounterPhases.dissolveBarriers(level, record);
        }
        if (record.state().phase() == LabyrinthPhase.CONQUERED) {
            EncounterPhases.openSpoils(level, record);
        }
    }

    private static void lock(ServerLevel level, MazeRecord record, BlockPos pos) {
        EldritchPlacement.restore(
                        level,
                        pos,
                        TTBlocks.ELDRITCH_LOCK.get(),
                        () -> TTBlocks.ELDRITCH_LOCK
                                .get()
                                .defaultBlockState()
                                .setValue(BlockEldritchLock.FACING, mountFacing(level, pos)),
                        TTBlockEntities.ELDRITCH_LOCK.get())
                .ifPresent(lock -> {
                    lock.bind(record.plan().id());
                    if (record.state().phase() != LabyrinthPhase.SEALED) {
                        long chargedAt = record.state().encounter().chargedAt();
                        lock.showCharge(
                                chargedAt >= 0 ? chargedAt : record.state().phaseSince());
                    }
                });
    }

    private static Direction mountFacing(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(direction)).isAir()
                    && !level.getBlockState(pos.relative(direction.getOpposite()))
                            .isAir()) {
                return direction;
            }
        }
        return Direction.NORTH;
    }
}
