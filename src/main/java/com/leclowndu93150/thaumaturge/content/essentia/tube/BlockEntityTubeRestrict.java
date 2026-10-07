package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityTubeRestrict extends BlockEntityTube {
    public BlockEntityTubeRestrict(BlockPos pos, BlockState state) {
        super(TTBlockEntities.TUBE_RESTRICT.get(), pos, state);
    }

    @Override
    protected boolean restrictiveSuction() {
        return true;
    }
}
