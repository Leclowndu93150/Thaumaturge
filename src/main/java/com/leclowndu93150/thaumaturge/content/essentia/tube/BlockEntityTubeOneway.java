package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlockEntityTubeOneway extends BlockEntityTube {
    public BlockEntityTubeOneway(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_ONEWAY.get(), pos, state);
    }

    @Override
    protected boolean directionalSuction() {
        return true;
    }

    @Override
    protected boolean directionalEqualize() {
        return true;
    }

    @Override
    protected void setFacing(Direction direction) {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            level.setBlock(getBlockPos(), state.setValue(BlockStateProperties.FACING, direction), Block.UPDATE_ALL);
        }
        setChanged();
    }

    @Override
    public Direction facing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return super.facing();
    }
}
