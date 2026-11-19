package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class PottedEtherealBloomBlock extends FlowerPotBlock implements EntityBlock {
    public PottedEtherealBloomBlock(BlockBehaviour.Properties properties) {
        super(() -> (FlowerPotBlock) Blocks.FLOWER_POT, TTBlocks.ETHEREAL_BLOOM, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEtherealBloom(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TTBlockEntities.ETHEREAL_BLOOM.get()) return null;
        return (tickLevel, pos, tickState, bloom) -> ((BlockEntityEtherealBloom) bloom).serverTick(tickLevel, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        TTBlocks.ETHEREAL_BLOOM.get().animateTick(state, level, pos, random);
    }
}
