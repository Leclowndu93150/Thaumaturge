package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockPlantCinderpearl extends AbstractTCPlant {
    public static final MapCodec<BlockPlantCinderpearl> CODEC = simpleCodec(BlockPlantCinderpearl::new);

    public BlockPlantCinderpearl(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockPlantCinderpearl> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SAND) || state.is(BlockTags.DIRT) || state.is(BlockTags.TERRACOTTA);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextBoolean()) {
            double xr = pos.getX() + 0.5D + (random.nextFloat() - random.nextFloat()) * 0.1F;
            double yr = pos.getY() + 0.6D + (random.nextFloat() - random.nextFloat()) * 0.1F;
            double zr = pos.getZ() + 0.5D + (random.nextFloat() - random.nextFloat()) * 0.1F;
            level.addParticle(ParticleTypes.SMOKE, xr, yr, zr, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, xr, yr, zr, 0.0D, 0.0D, 0.0D);
        }
    }
}
