package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockPlantShimmerleaf extends AbstractTCPlant {
    public static final MapCodec<BlockPlantShimmerleaf> CODEC = simpleCodec(BlockPlantShimmerleaf::new);

    public BlockPlantShimmerleaf(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockPlantShimmerleaf> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return isGrassOrDirt(state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double xr = pos.getX() + 0.5D + random.nextGaussian() * 0.1D;
            double yr = pos.getY() + 0.4D + random.nextGaussian() * 0.1D;
            double zr = pos.getZ() + 0.5D + random.nextGaussian() * 0.1D;
            level.addParticle(ParticleTypes.END_ROD, xr, yr, zr, random.nextGaussian() * 0.01D, random.nextGaussian() * 0.01D, random.nextGaussian() * 0.01D);
        }
    }
}
