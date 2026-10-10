package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
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

    private static final float KEY_VOLUME = 1.0F;
    private static final float KEY_PITCH = 1.0F;

    public BlockTubeFilter(BlockBehaviour.Properties properties) {
        super(properties, TubeGeometry.FILTER);
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
        return tubeTicker(level, type, TTBlockEntities.TUBE_FILTER.get());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isSecondaryUseActive() || !(level.getBlockEntity(pos) instanceof BlockEntityTubeFilter filter) || filter.aspectFilter() == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            filter.setAspectFilter(null);
            playKey(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeFilter filter) || filter.aspectFilter() != null) {
            return InteractionResult.PASS;
        }
        IItemEssentia contents = stack.getCapability(EssentiaCapabilities.CONTAINER);
        if (contents == null) {
            return InteractionResult.PASS;
        }
        AspectList aspects = contents.getAspects();
        if (aspects.isEmpty()) {
            return InteractionResult.PASS;
        }
        ResourceKey<IAspect> key = aspects.entries().getFirst().aspect().unwrapKey().orElse(null);
        if (key == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            filter.setAspectFilter(key);
            playKey(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private static void playKey(Level level, BlockPos pos) {
        level.playSound(null, pos, TTSounds.KEY.get(), SoundSource.BLOCKS, KEY_VOLUME, KEY_PITCH);
    }
}
