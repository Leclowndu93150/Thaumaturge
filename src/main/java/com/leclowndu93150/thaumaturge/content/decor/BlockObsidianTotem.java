package com.leclowndu93150.thaumaturge.content.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockObsidianTotem extends Block {
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public BlockObsidianTotem(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        return defaultBlockState().setValue(UP, isTotem(context.getLevel().getBlockState(pos.above()))).setValue(DOWN, isTotem(context.getLevel().getBlockState(pos.below())));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == Direction.UP) {
            return state.setValue(UP, isTotem(neighbourState));
        }
        if (directionToNeighbour == Direction.DOWN) {
            return state.setValue(DOWN, isTotem(neighbourState));
        }
        return state;
    }

    public static boolean isTotem(BlockState state) {
        return state.getBlock() instanceof BlockObsidianTotem;
    }

    public static BlockState shapedState(BlockState state, BlockGetter level, BlockPos pos) {
        return state.setValue(UP, isTotem(level.getBlockState(pos.above()))).setValue(DOWN, isTotem(level.getBlockState(pos.below())));
    }
}
