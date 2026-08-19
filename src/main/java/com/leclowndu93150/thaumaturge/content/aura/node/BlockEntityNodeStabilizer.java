package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityNodeStabilizer extends BlockEntity {
    public static final int MAX_COUNT = 37;

    public int count;

    public BlockEntityNodeStabilizer(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NODE_STABILIZER.get(), pos, state);
    }

    public boolean isAdvanced() {
        return getBlockState().getBlock() instanceof BlockNodeStabilizer stabilizer && stabilizer.isAdvanced();
    }

    public void clientTick(Level clientLevel, BlockPos pos) {
        boolean active = clientLevel.getBlockEntity(pos.above()) instanceof BlockEntityNode node && !(node instanceof BlockEntityJarNode) && !clientLevel.hasNeighborSignal(pos);
        if (active) {
            if (count < MAX_COUNT) {
                count++;
            }
        } else if (count > 0) {
            count--;
        }
    }
}
