package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.content.world.plant.AbstractTTPlant;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public final class BlockEtherealBloom extends AbstractTTPlant implements EntityBlock {
    public static final MapCodec<BlockEtherealBloom> CODEC = simpleCodec(BlockEtherealBloom::new);

    private static final int PARTICLE_CHANCE = 2;
    private static final double PARTICLE_Y = 0.45D;
    private static final double PARTICLE_SPREAD_XZ = 0.18D;
    private static final double PARTICLE_SPREAD_Y = 0.12D;
    private static final double PARTICLE_RISE = 0.008D;

    public BlockEtherealBloom(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEtherealBloom> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEtherealBloom(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TTBlockEntities.ETHEREAL_BLOOM.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, bloom) -> ((BlockEntityEtherealBloom) bloom).serverTick(tickLevel, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(PARTICLE_CHANCE) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5D + random.nextGaussian() * PARTICLE_SPREAD_XZ, pos.getY() + PARTICLE_Y + random.nextGaussian() * PARTICLE_SPREAD_Y,
                    pos.getZ() + 0.5D + random.nextGaussian() * PARTICLE_SPREAD_XZ, 0.0D, PARTICLE_RISE, 0.0D);
        }
    }
}
