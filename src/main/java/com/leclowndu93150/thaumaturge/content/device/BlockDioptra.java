package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BlockDioptra extends BaseEntityBlock {
    public static final MapCodec<BlockDioptra> CODEC = simpleCodec(BlockDioptra::new);

    private static final int CENTER_INDEX = BlockEntityDioptra.GRID_SIZE * (BlockEntityDioptra.GRID_SIZE / 2) + BlockEntityDioptra.GRID_SIZE / 2;
    private static final float SIGNAL_SPAN = 14.0F;

    public BlockDioptra(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.ENABLED, true));
    }

    @Override
    protected MapCodec<BlockDioptra> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.ENABLED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityDioptra(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        level.setBlock(pos, state.cycle(BlockStateProperties.ENABLED), Block.UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityDioptra dioptra)) {
            return 0;
        }
        float ratio = dioptra.gridValue(CENTER_INDEX) / (float) BlockEntityDioptra.STEPS;
        return ratio > 0.0F ? (int) (ratio * SIGNAL_SPAN) + 1 : 0;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.DIOPTRA.get(), BlockEntityDioptra::serverTick);
    }
}
