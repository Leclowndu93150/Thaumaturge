package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockPlantVishroom extends AbstractTCPlant {
    public static final MapCodec<BlockPlantVishroom> CODEC = simpleCodec(BlockPlantVishroom::new);

    public BlockPlantVishroom(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockPlantVishroom> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.MYCELIUM) || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.STONE);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && level.getRandom().nextInt(5) == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double xr = pos.getX() + 0.5D + (random.nextFloat() - random.nextFloat()) * 0.4D;
            double yr = pos.getY() + 0.3D;
            double zr = pos.getZ() + 0.5D + (random.nextFloat() - random.nextFloat()) * 0.4D;
            level.addParticle(ParticleTypes.WITCH, xr, yr, zr, 0.0D, 0.0D, 0.0D);
        }
    }
}
