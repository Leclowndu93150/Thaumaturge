package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityEldritchCap extends BlockEntity {
    public BlockEntityEldritchCap(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ELDRITCH_CAP.get(), pos, state);
    }
}
