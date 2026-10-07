package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public final class BlockEssentiaReservoir extends BaseEntityBlock implements IInteractWithCaster {
    public static final MapCodec<BlockEssentiaReservoir> CODEC = simpleCodec(BlockEssentiaReservoir::new);
    private static final int MAX_COMPARATOR_STEP = 14;

    public BlockEssentiaReservoir(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.DOWN));
    }

    @Override
    protected MapCodec<BlockEssentiaReservoir> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.FACING, rotation.rotate(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEssentiaReservoir(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, TTBlockEntities.ESSENTIA_RESERVOIR.get(), BlockEntityEssentiaReservoir::serverTick);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityEssentiaReservoir reservoir)) {
            return 0;
        }
        int amount = reservoir.getStoredAmount();
        return amount <= 0 ? 0 : 1 + (int) Math.floor(amount / (double) BlockEntityEssentiaReservoir.CAPACITY * MAX_COMPARATOR_STEP);
    }

    @Override
    public boolean onCasterRightClick(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (level.isClientSide()) {
            return true;
        }
        BlockState state = level.getBlockState(pos);
        Direction facing = player.isShiftKeyDown() ? side.getOpposite() : side;
        if (state.getValue(BlockStateProperties.FACING) != facing) {
            level.setBlock(pos, state.setValue(BlockStateProperties.FACING, facing), Block.UPDATE_ALL);
            level.invalidateCapabilities(pos);
            BlockEssentiaTransport.refreshConnectionsAround(level, pos);
        }
        player.swing(hand);
        return true;
    }
}
