package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BlockTubeFilter extends BlockTube {
    public static final MapCodec<BlockTubeFilter> CODEC = simpleCodec(BlockTubeFilter::new);

    public BlockTubeFilter(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockTube> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityTubeFilter(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide())
            return null;
        return createTickerHelper(type, TCBlockEntities.TUBE_FILTER.get(), (lvl, pos, st, tube) -> tube.tickServer(lvl, pos, st));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeFilter filter))
            return InteractionResult.PASS;
        if (player.isSecondaryUseActive() && filter.aspectFilter() != null) {
            if (!level.isClientSide()) {
                filter.setAspectFilter(null);
                level.playSound(null, pos, TCSounds.KEY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeFilter filter))
            return InteractionResult.PASS;
        if (filter.aspectFilter() != null)
            return InteractionResult.PASS;
        IEssentiaContainerItem container = stack.getCapability(EssentiaCapabilities.CONTAINER);
        if (container == null)
            return InteractionResult.PASS;
        if (container.getAspects(stack) == null || container.getAspects(stack).isEmpty())
            return InteractionResult.PASS;
        AspectInstance first = container.getAspects(stack).entries().get(0);
        ResourceKey<IAspect> key = first.aspect().unwrapKey().orElse(null);
        if (key == null)
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            filter.setAspectFilter(key);
            level.playSound(null, pos, TCSounds.KEY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
}
