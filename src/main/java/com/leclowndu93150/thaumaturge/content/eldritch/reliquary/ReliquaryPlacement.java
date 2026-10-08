package com.leclowndu93150.thaumaturge.content.eldritch.reliquary;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.content.eldritch.EldritchPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeView;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public final class ReliquaryPlacement {
    private ReliquaryPlacement() {}

    public static void ensureKeyRoom(ServerLevel level, MazeRecord record) {
        record.plan()
                .landmarks()
                .point(LabyrinthLandmarks.KEY)
                .ifPresent(pos -> ensure(
                        level,
                        record,
                        pos,
                        () -> new MazeView(record)
                                .directionToward(LabyrinthLandmarks.ARRIVAL, pos)
                                .orElse(Direction.NORTH),
                        ReliquaryRole.KEY_ROOM));
    }

    public static void ensureBoss(ServerLevel level, MazeRecord record) {
        Optional<BlockPos> reward = record.plan().landmarks().point(LabyrinthLandmarks.REWARD);
        if (reward.isEmpty()) {
            return;
        }
        ensure(
                level,
                record,
                reward.get(),
                () -> record.plan()
                        .landmarks()
                        .point(LabyrinthLandmarks.BOSS_DOOR)
                        .map(door -> toward(reward.get(), door))
                        .orElse(Direction.NORTH),
                ReliquaryRole.BOSS);
    }

    public static void stamp(
            MarkerStampContext context,
            BlockPos pos,
            Direction facing,
            ReliquaryRole role,
            Optional<ResourceKey<LootTable>> loot) {
        BlockState state = TTBlocks.ELDRITCH_RELIQUARY
                .get()
                .defaultBlockState()
                .setValue(BlockEldritchReliquary.FACING, context.orient(facing));
        context.placeEntity(pos, state, TTBlockEntities.ELDRITCH_RELIQUARY.get())
                .ifPresent(reliquary -> reliquary.configure(context.maze(), role, loot));
    }

    private static void ensure(
            ServerLevel level, MazeRecord record, BlockPos pos, Supplier<Direction> facing, ReliquaryRole role) {
        EldritchPlacement.restore(
                        level,
                        pos,
                        TTBlocks.ELDRITCH_RELIQUARY.get(),
                        () -> TTBlocks.ELDRITCH_RELIQUARY
                                .get()
                                .defaultBlockState()
                                .setValue(BlockEldritchReliquary.FACING, facing.get()),
                        TTBlockEntities.ELDRITCH_RELIQUARY.get())
                .ifPresent(reliquary -> reliquary.configure(record.plan().id(), role, Optional.empty()));
    }

    private static Direction toward(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
