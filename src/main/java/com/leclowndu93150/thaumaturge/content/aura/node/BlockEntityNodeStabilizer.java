package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityNodeStabilizer extends BlockEntity {
    public static final int MAX_COUNT = 36;

    public int count;

    public BlockEntityNodeStabilizer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.NODE_STABILIZER.get(), pos, state);
    }

    public boolean isAdvanced() {
        return getBlockState().getBlock() instanceof BlockNodeStabilizer stabilizer && stabilizer.isAdvanced();
    }

    public void clientTick(Level level, BlockPos pos) {
        if (isEngaged(level, pos)) {
            count = Math.min(MAX_COUNT, count + 1);
        } else if (count > 0) {
            count--;
        }
    }

    private static boolean isEngaged(Level level, BlockPos pos) {
        return !level.hasNeighborSignal(pos) && level.getBlockEntity(pos.above()) instanceof BlockEntityNode node && node.allowLock();
    }
}
