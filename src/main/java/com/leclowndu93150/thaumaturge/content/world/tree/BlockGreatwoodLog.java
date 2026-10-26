package com.leclowndu93150.thaumaturge.content.world.tree;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.Nullable;

public final class BlockGreatwoodLog extends RotatedPillarBlock {
    public static final MapCodec<BlockGreatwoodLog> CODEC = simpleCodec(BlockGreatwoodLog::new);

    public BlockGreatwoodLog(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TrunkCorner.PROPERTY, TrunkCorner.NONE));
    }

    @Override
    public MapCodec<BlockGreatwoodLog> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TrunkCorner.PROPERTY);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null
                ? null
                : state.setValue(TrunkCorner.PROPERTY, cornerAt(context.getLevel(), context.getClickedPos(), state));
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction directionToNeighbour,
            BlockState neighbourState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighbourPos) {
        if (directionToNeighbour.getAxis().isHorizontal()) {
            return state.setValue(TrunkCorner.PROPERTY, cornerAt(level, pos, state));
        }
        return state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(this) || oldState.getValue(AXIS) != state.getValue(AXIS)) {
            refreshNearby(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!newState.is(this)) {
            refreshNearby(level, pos);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return super.rotate(state, rotation)
                .setValue(
                        TrunkCorner.PROPERTY,
                        state.getValue(TrunkCorner.PROPERTY).rotate(rotation));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(
                TrunkCorner.PROPERTY, state.getValue(TrunkCorner.PROPERTY).mirror(mirror));
    }

    private TrunkCorner cornerAt(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getValue(AXIS) != Direction.Axis.Y) {
            return TrunkCorner.NONE;
        }
        if (isSquare(level, pos, 1, 1)) {
            return TrunkCorner.NORTH_WEST;
        }
        if (isSquare(level, pos, -1, 1)) {
            return TrunkCorner.NORTH_EAST;
        }
        if (isSquare(level, pos, 1, -1)) {
            return TrunkCorner.SOUTH_WEST;
        }
        if (isSquare(level, pos, -1, -1)) {
            return TrunkCorner.SOUTH_EAST;
        }
        return TrunkCorner.NONE;
    }

    private boolean isSquare(LevelReader level, BlockPos pos, int dx, int dz) {
        if (!isTrunk(level.getBlockState(pos.offset(dx, 0, 0)))
                || !isTrunk(level.getBlockState(pos.offset(0, 0, dz)))
                || !isTrunk(level.getBlockState(pos.offset(dx, 0, dz)))) {
            return false;
        }
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                BlockPos corner = pos.offset(x * dx, 0, z * dz);
                if (isTrunk(level.getBlockState(corner.offset((x == 0 ? -1 : 1) * dx, 0, 0)))
                        || isTrunk(level.getBlockState(corner.offset(0, 0, (z == 0 ? -1 : 1) * dz)))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void refreshNearby(Level level, BlockPos changedPos) {
        if (level.isClientSide) {
            return;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = changedPos.offset(dx, 0, dz);
                BlockState state = level.getBlockState(pos);
                if (state.is(this)) {
                    TrunkCorner corner = cornerAt(level, pos, state);
                    if (state.getValue(TrunkCorner.PROPERTY) != corner) {
                        level.setBlock(
                                pos,
                                state.setValue(TrunkCorner.PROPERTY, corner),
                                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
        }
    }

    private boolean isTrunk(BlockState state) {
        return state.is(this) && state.getValue(AXIS) == Direction.Axis.Y;
    }
}
