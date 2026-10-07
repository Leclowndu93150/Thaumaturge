package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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

public class BlockTube extends BlockEssentiaTransport {
    public static final MapCodec<BlockTube> CODEC = simpleCodec(BlockTube::new);

    public BlockTube(BlockBehaviour.Properties properties) {
        this(properties, TubeGeometry.PIPE);
    }

    protected BlockTube(BlockBehaviour.Properties properties, TubeGeometry geometry) {
        super(properties, geometry);
    }

    @Override
    protected MapCodec<? extends BlockTube> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityTube(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide())
            return null;
        return createTickerHelper(type, TTBlockEntities.TUBE.get(), (lvl, pos, st, tube) -> tube.tickServer(lvl, pos, st));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof BlockEntityTube tube) {
            tube.setFacingForPlacement(placer);
            refreshConnectionsAround(level, pos);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isSecondaryUseActive())
            return InteractionResult.PASS;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTube tube))
            return InteractionResult.PASS;
        int subHit = resolveSubHit(state, hit, pos);
        if (subHit == TubeGeometry.CORE_HIT && !tube.isSideOpen(hit.getDirection())) {
            subHit = hit.getDirection().ordinal();
        }
        if (tube.handleCasterClick(subHit)) {
            tube.playToolSound(level, pos);
            player.swing(player.getUsedItemHand());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
