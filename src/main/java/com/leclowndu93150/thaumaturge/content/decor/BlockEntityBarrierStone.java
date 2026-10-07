package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityBarrierStone extends BlockEntity {
    private static final int REPEL_PERIOD = 5;
    private static final int RAISE_PERIOD = 100;
    private static final int WALL_HEIGHT = 2;
    private static final int GUARDED_HEIGHT = 3;
    private static final double GUARD_MARGIN = 0.1;
    private static final double SHOVE_SPEED = 0.2;
    private static final double SHOVE_SINK = 0.1;

    public BlockEntityBarrierStone(BlockPos pos, BlockState state) {
        super(TTBlockEntities.BARRIER_STONE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityBarrierStone stone) {
        long phase = level.getGameTime() + Math.floorMod(pos.asLong(), RAISE_PERIOD);
        if (phase % REPEL_PERIOD == 0 && !level.hasNeighborSignal(pos)) {
            shoveAirborneMobs(level, pos);
        }
        if (phase % RAISE_PERIOD == 0) {
            raiseWall(level, pos);
        }
    }

    private static void shoveAirborneMobs(Level level, BlockPos pos) {
        AABB guarded = new AABB(pos).expandTowards(0.0, GUARDED_HEIGHT - 1, 0.0).inflate(GUARD_MARGIN);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, guarded, BlockEntityBarrierStone::isRepelled)) {
            mob.push(Vec3.directionFromRotation(0.0F, mob.getYRot()).scale(-SHOVE_SPEED).subtract(0.0, SHOVE_SINK, 0.0));
        }
    }

    private static boolean isRepelled(LivingEntity entity) {
        return !(entity instanceof Player) && !entity.onGround();
    }

    private static void raiseWall(Level level, BlockPos pos) {
        BlockState wall = TTBlocks.BARRIER.get().defaultBlockState();
        for (int height = 1; height <= WALL_HEIGHT; height++) {
            BlockPos target = pos.above(height);
            if (level.isEmptyBlock(target)) {
                level.setBlock(target, wall, Block.UPDATE_ALL);
            }
        }
    }
}
