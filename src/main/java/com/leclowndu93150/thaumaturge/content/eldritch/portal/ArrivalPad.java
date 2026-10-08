package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazePlan;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world.RoomStamper;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

public final class ArrivalPad {
    private static final int SEARCH_HORIZONTAL = 2;
    private static final int SEARCH_VERTICAL = 1;

    private ArrivalPad() {}

    public static Arrival prepare(ServerLevel outer, MazeRecord record) {
        MazePlan plan = record.plan();
        BlockPos anchor = plan.landmarks()
                .point(LabyrinthLandmarks.ARRIVAL)
                .orElseGet(() -> plan.geometry()
                        .cellCenter(plan.geometry().width() / 2, plan.geometry().depth() / 2));
        ensureStamped(outer, record, anchor);
        BlockPos spot = SafeSpot.near(outer, anchor, SEARCH_HORIZONTAL, SEARCH_VERTICAL)
                .orElseGet(() -> repair(outer, record, anchor));
        float yaw = plan.landmarks()
                .point(LabyrinthLandmarks.ENTRY_PORTAL)
                .filter(portal -> !portal.equals(spot))
                .map(portal -> facing(portal, spot))
                .orElse(0.0F);
        return new Arrival(Vec3.atBottomCenterOf(spot), yaw);
    }

    private static void ensureStamped(ServerLevel outer, MazeRecord record, BlockPos anchor) {
        LevelChunk chunk = outer.getChunkAt(anchor);
        LabyrinthService.runtime(outer)
                .ifPresent(runtime -> RoomStamper.ensureStamped(outer, chunk, record.plan(), runtime));
    }

    private static BlockPos repair(ServerLevel outer, MazeRecord record, BlockPos anchor) {
        if (!ThaumaturgeServerConfig.LABYRINTH.allowArrivalRepair.get()) {
            return anchor;
        }
        BlockPos floor = anchor.below();
        BlockState floorState = outer.getBlockState(floor);
        if (!floorState.isFaceSturdy(outer, floor, Direction.UP) || floorState.is(TTBlockTags.UNSAFE_LANDING)) {
            outer.setBlock(floor, pad(outer, record), Block.UPDATE_ALL);
        }
        outer.setBlock(anchor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        outer.setBlock(anchor.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return anchor;
    }

    private static BlockState pad(ServerLevel outer, MazeRecord record) {
        return LabyrinthService.definition(outer.getServer(), record)
                .map(definition -> definition.gameplay().arrivalPad())
                .orElseGet(() -> TTBlocks.STONE_ELDRITCH_TILE.get().defaultBlockState());
    }

    private static float facing(BlockPos from, BlockPos to) {
        return (float) Mth.atan2(-(to.getX() - from.getX()), to.getZ() - from.getZ()) * Mth.RAD_TO_DEG;
    }

    public record Arrival(Vec3 pos, float yaw) {}
}
