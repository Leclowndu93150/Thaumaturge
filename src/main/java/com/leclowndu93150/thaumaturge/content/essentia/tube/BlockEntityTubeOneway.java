package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    public Direction facing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return super.facing();
    }

    @Override
    public boolean rotateFacing() {
        if (level == null) return false;
        Direction current = facing();
        Direction next = findNextFacing(current, true);
        if (next == null) {
            next = findNextFacing(current, false);
        }
        if (next == null) return false;
        level.setBlock(getBlockPos(), getBlockState().setValue(BlockStateProperties.FACING, next), 3);
        return true;
    }

    private Direction findNextFacing(Direction current, boolean requireNeighbour) {
        int start = current.ordinal();
        for (int offset = 1; offset < Direction.values().length; offset++) {
            Direction candidate = Direction.values()[(start + offset) % Direction.values().length];
            if (isSideOpen(candidate) && (!requireNeighbour || hasTransportNeighbour(candidate))) {
                return candidate;
            }
        }
        return null;
    }
}
