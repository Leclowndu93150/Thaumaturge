package com.leclowndu93150.thaumaturge.content.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStoneTT extends Block {
    private final boolean unbreakable;

    public BlockStoneTT(BlockBehaviour.Properties properties, boolean unbreakable) {
        super(properties);
        this.unbreakable = unbreakable;
    }

    public BlockStoneTT(BlockBehaviour.Properties properties) {
        this(properties, false);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return !this.unbreakable;
    }
}
