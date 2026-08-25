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
    public boolean canInputFrom(Direction face) {
        return face == facing().getOpposite() && super.canInputFrom(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face != facing().getOpposite() && super.canOutputTo(face);
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
        Direction next = findNextFacing(current);
        if (next == null) return false;
        level.setBlock(getBlockPos(), getBlockState().setValue(BlockStateProperties.FACING, next), 3);
        return true;
    }

    private Direction findNextFacing(Direction current) {
        int start = current.ordinal();
        for (int offset = 1; offset < Direction.values().length; offset++) {
            Direction candidate = Direction.values()[(start + offset) % Direction.values().length];
            Direction connectionSide = candidate.getOpposite();
            if (isSideOpen(connectionSide) && hasTransportNeighbour(connectionSide)) {
                return candidate;
            }
        }
        return null;
    }
}
