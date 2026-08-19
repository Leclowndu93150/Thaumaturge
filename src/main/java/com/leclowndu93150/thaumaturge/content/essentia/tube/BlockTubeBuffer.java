package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BlockTubeBuffer extends BlockEssentiaTransport {
    public static final MapCodec<BlockTubeBuffer> CODEC = simpleCodec(BlockTubeBuffer::new);

    public BlockTubeBuffer(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockTubeBuffer> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityTubeBuffer(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeBuffer buffer))
            return 0;
        int size = buffer.visSize();
        if (size <= 0)
            return 0;
        float ratio = size / (float) BlockEntityTubeBuffer.MAX_AMOUNT;
        return Math.min(15, (int) Math.floor(ratio * 14.0F) + 1);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide())
            return null;
        return createTickerHelper(type, TCBlockEntities.TUBE_BUFFER.get(), (lvl, pos, st, buffer) -> buffer.tickServer(lvl, pos, st));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof BlockEntityTubeBuffer buffer) {
            buffer.setFacingForPlacement(placer);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isSecondaryUseActive())
            return InteractionResult.PASS;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeBuffer buffer))
            return InteractionResult.PASS;
        int subHit = BlockTube.resolveSubHit(hit, pos);
        if (buffer.handleCasterClick(subHit, player.isShiftKeyDown())) {
            player.swing(player.getUsedItemHand());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
