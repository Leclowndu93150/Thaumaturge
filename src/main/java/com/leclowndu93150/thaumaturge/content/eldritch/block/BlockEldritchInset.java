package com.leclowndu93150.thaumaturge.content.eldritch.block;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockEldritchInset extends DropExperienceBlock {
    public static final Map<Direction, BooleanProperty> EXPOSED = PipeBlock.PROPERTY_BY_DIRECTION;

    public BlockEldritchInset(IntProvider xpRange, Properties properties) {
        super(xpRange, properties);
        BlockState state = this.stateDefinition.any();
        for (BooleanProperty property : EXPOSED.values()) {
            state = state.setValue(property, false);
        }
        this.registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        EXPOSED.values().forEach(builder::add);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        for (Direction dir : Direction.values()) {
            BlockPos neighbourPos = context.getClickedPos().relative(dir);
            BlockState neighbour = context.getLevel().getBlockState(neighbourPos);
            state = state.setValue(EXPOSED.get(dir), !neighbour.isFaceSturdy(context.getLevel(), neighbourPos, dir.getOpposite()));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return state.setValue(EXPOSED.get(directionToNeighbour), !neighbourState.isFaceSturdy(level, neighbourPos, directionToNeighbour.getOpposite()));
    }
}
