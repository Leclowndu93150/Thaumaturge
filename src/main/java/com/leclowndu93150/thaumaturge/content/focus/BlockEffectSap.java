package com.leclowndu93150.thaumaturge.content.focus;

import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockEffectSap extends Block {
    public static final MapCodec<BlockEffectSap> CODEC = simpleCodec(BlockEffectSap::new);

    private static final int EFFECT_DURATION_TICKS = 40;
    private static final int SLOWNESS_AMPLIFIER = 1;
    private static final int HUNGER_AMPLIFIER = 1;
    private static final int SOUND_ONE_IN = 50;
    private static final float SOUND_VOLUME = 0.25F;
    private static final float SPARK_BASE_SCALE = 3.0F;
    private static final float SPARK_SCALE_SPREAD = 6.0F;
    private static final float SPARK_HEIGHT = 0.1515F;

    public BlockEffectSap(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEffectSap> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level.isClientSide() || entity instanceof IEldritchMob) {
            return;
        }
        if (!(entity instanceof LivingEntity living) || living.hasEffect(MobEffects.WITHER)) {
            return;
        }
        living.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_DURATION_TICKS, 0, true, true));
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECT_DURATION_TICKS, SLOWNESS_AMPLIFIER, true, true));
        living.addEffect(new MobEffectInstance(MobEffects.HUNGER, EFFECT_DURATION_TICKS, HUNGER_AMPLIFIER, true, true));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float h = random.nextFloat() * 0.33F;
        spawnSpark(level, pos.getX() + random.nextFloat(), pos.getY() + SPARK_HEIGHT + h / 2.0F, pos.getZ() + random.nextFloat(), SPARK_BASE_SCALE + h * SPARK_SCALE_SPREAD,
                0.3F - random.nextFloat() * 0.1F, 0.0F, 0.5F + random.nextFloat() * 0.2F, 1.0F);
        if (random.nextInt(SOUND_ONE_IN) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TCSounds.JACOBS.get(), SoundSource.AMBIENT, SOUND_VOLUME, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F, false);
        }
    }

    private static void spawnSpark(Level level, double x, double y, double z, float size, float r, float g, float b, float alpha) {
        RandomSource rand = level.getRandom();
        level.addParticle(new SparkParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), alpha, size), x, y, z, 0.0, 0.0, 0.0);
    }
}
