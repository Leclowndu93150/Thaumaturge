package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockJarBrain extends BaseEntityBlock {
    public static final MapCodec<BlockJarBrain> CODEC = simpleCodec(BlockJarBrain::new);

    private static final int EAT_DELAY_TICKS = 40;
    private static final int MAX_RELEASE = 63;
    private static final float CLICK_VOLUME = 0.2F;
    private static final float CLICK_PITCH = 1.1F;
    private static final double JAR_TOP = 0.8;
    private static final double SPARKLE_SPREAD = 0.25;
    private static final double SPARKLE_RISE = 0.02;
    private static final int SPARKLE_COLOR = 0xFF9BFF6A;
    private static final float SPARKLE_SCALE = 0.5F;
    private static final float SPARKLE_DECAY = 1.0F;
    private static final float SPARKLE_GRAVITY = 0.0F;
    private static final int SPARKLE_BASE_AGE = 16;
    private static final float ENCHANTING_POWER_PER_JAR = 5.0F;

    public BlockJarBrain(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<BlockJarBrain> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AbstractBlockJar.SHAPE;
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return ENCHANTING_POWER_PER_JAR;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AbstractBlockJar.SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityJarBrain(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJarBrain brain)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        int release = server.getRandom().nextInt(Math.min(brain.xp(), MAX_RELEASE) + 1);
        if (release > 0) {
            brain.setXp(brain.xp() - release);
            ExperienceOrb.award(server, Vec3.atCenterOf(pos), release);
        }
        brain.setEatDelay(EAT_DELAY_TICKS);
        level.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, CLICK_VOLUME, CLICK_PITCH);
        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJarBrain brain)
                || brain.xp() < BlockEntityJarBrain.XP_MAX) {
            return;
        }
        Vec3 top = Vec3.atBottomCenterOf(pos)
                .add(random.nextGaussian() * SPARKLE_SPREAD, JAR_TOP, random.nextGaussian() * SPARKLE_SPREAD);
        SparkleParticleOptions sparkle = new SparkleParticleOptions(
                SPARKLE_COLOR, SPARKLE_SCALE, 0, SPARKLE_DECAY, SPARKLE_GRAVITY, SPARKLE_BASE_AGE, true);
        level.addParticle(sparkle, top.x, top.y, top.z, 0.0, SPARKLE_RISE, 0.0);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, TTBlockEntities.JAR_BRAIN.get(), BlockEntityJarBrain::clientTick)
                : createTickerHelper(type, TTBlockEntities.JAR_BRAIN.get(), BlockEntityJarBrain::serverTick);
    }
}
