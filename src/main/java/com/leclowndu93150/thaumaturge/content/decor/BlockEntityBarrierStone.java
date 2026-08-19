package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BlockEntityBarrierStone extends BlockEntity {
    private static final int PUSH_INTERVAL = 5;
    private static final int BARRIER_INTERVAL = 100;
    private static final double PUSH_STRENGTH = 0.2;
    private static final double PUSH_DOWN = -0.1;

    private int count;

    public BlockEntityBarrierStone(BlockPos pos, BlockState state) {
        super(TCBlockEntities.BARRIER_STONE.get(), pos, state);
    }

    public boolean gettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    public void serverTick(Level level, BlockPos pos) {
        if (count == 0) {
            count = level.getRandom().nextInt(BARRIER_INTERVAL);
        }
        if (count % PUSH_INTERVAL == 0 && !gettingPower()) {
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1).inflate(0.1));
            for (LivingEntity entity : targets) {
                if (!entity.onGround() && !(entity instanceof Player)) {
                    entity.push(-Mth.sin((entity.getYRot() + 180.0F) * Mth.DEG_TO_RAD) * PUSH_STRENGTH, PUSH_DOWN, Mth.cos((entity.getYRot() + 180.0F) * Mth.DEG_TO_RAD) * PUSH_STRENGTH);
                }
            }
        }
        if (++count % BARRIER_INTERVAL == 0) {
            BlockState barrier = TCBlocks.BARRIER.get().defaultBlockState();
            if (level.getBlockState(pos.above(1)) != barrier && level.isEmptyBlock(pos.above(1))) {
                level.setBlock(pos.above(1), barrier, 3);
            }
            if (level.getBlockState(pos.above(2)) != barrier && level.isEmptyBlock(pos.above(2))) {
                level.setBlock(pos.above(2), barrier, 3);
            }
        }
    }
}
