package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.content.world.plant.AbstractTCPlant;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEtherealBloom extends AbstractTCPlant implements EntityBlock {
    public static final MapCodec<BlockEtherealBloom> CODEC = simpleCodec(BlockEtherealBloom::new);

    public BlockEtherealBloom(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEtherealBloom> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isFaceSturdy(level, pos, net.minecraft.core.Direction.UP);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEtherealBloom(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TCBlockEntities.ETHEREAL_BLOOM.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) -> BlockEntityEtherealBloom.serverTick(
                tickerLevel, pos, tickerState, (BlockEntityEtherealBloom) blockEntity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    pos.getX() + 0.5D + random.nextGaussian() * 0.18D,
                    pos.getY() + 0.45D + random.nextGaussian() * 0.12D,
                    pos.getZ() + 0.5D + random.nextGaussian() * 0.18D,
                    0.0D,
                    0.008D,
                    0.0D);
        }
    }
}
