package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.DIRT) || state.is(Blocks.TERRACOTTA) || state.is(Blocks.WHITE_TERRACOTTA) || state.is(Blocks.ORANGE_TERRACOTTA)
                || state.is(Blocks.MAGENTA_TERRACOTTA) || state.is(Blocks.LIGHT_BLUE_TERRACOTTA) || state.is(Blocks.YELLOW_TERRACOTTA) || state.is(Blocks.LIME_TERRACOTTA)
                || state.is(Blocks.PINK_TERRACOTTA) || state.is(Blocks.GRAY_TERRACOTTA) || state.is(Blocks.LIGHT_GRAY_TERRACOTTA) || state.is(Blocks.CYAN_TERRACOTTA)
                || state.is(Blocks.PURPLE_TERRACOTTA) || state.is(Blocks.BLUE_TERRACOTTA) || state.is(Blocks.BROWN_TERRACOTTA) || state.is(Blocks.GREEN_TERRACOTTA) || state.is(Blocks.RED_TERRACOTTA)
                || state.is(Blocks.BLACK_TERRACOTTA);
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
