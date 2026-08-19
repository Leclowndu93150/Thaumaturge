package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockPavingStone extends BaseEntityBlock {
    public static final MapCodec<BlockPavingStone> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Codec.BOOL.fieldOf("barrier").forGetter(block -> block.barrier), propertiesCodec()).apply(instance, BlockPavingStone::new));

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    private static final int SPEED_DURATION = 40;
    private static final int SPEED_AMPLIFIER = 1;
    private static final int RUNE_DURATION_POWERED = 20;
    private static final int RUNE_DURATION_ACTIVE = 24;
    private static final float RUNE_GRAVITY = -0.02F;

    private final boolean barrier;

    public BlockPavingStone(boolean barrier, BlockBehaviour.Properties properties) {
        super(properties);
        this.barrier = barrier;
    }

    @Override
    protected MapCodec<BlockPavingStone> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && !barrier && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SPEED, SPEED_DURATION, SPEED_AMPLIFIER, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, SPEED_DURATION, 0, false, false));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return barrier ? new BlockEntityBarrierStone(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!barrier || level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TCBlockEntities.BARRIER_STONE.get(), (tickLevel, pos, tickState, barrierStone) -> barrierStone.serverTick(tickLevel, pos));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!barrier) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            for (int a = 0; a < 4; a++) {
                spawnRune(level, pos, 0.2F + random.nextFloat() * 0.4F, random.nextFloat() * 0.3F, 0.8F + random.nextFloat() * 0.2F, RUNE_DURATION_POWERED, RUNE_GRAVITY);
            }
            return;
        }
        BlockState barrierState = TCBlocks.BARRIER.get().defaultBlockState();
        if (level.getBlockState(pos.above(1)) == barrierState || level.getBlockState(pos.above(2)) == barrierState) {
            for (int a = 0; a < 6; a++) {
                spawnRune(level, pos, 0.9F + random.nextFloat() * 0.1F, random.nextFloat() * 0.3F, random.nextFloat() * 0.3F, RUNE_DURATION_ACTIVE, RUNE_GRAVITY);
            }
            return;
        }
        List<Entity> nearby = level.getEntities(null, new AABB(pos).inflate(1.0));
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                level.addParticle(new BlockRunesParticleOptions(0.6F + random.nextFloat() * 0.4F, 0.0F, 0.3F + random.nextFloat() * 0.7F, RUNE_DURATION_POWERED, 0.0F, false), pos.getX() + 0.5,
                        pos.getY() + 0.6F + random.nextFloat() * Math.max(0.8F, entity.getEyeHeight()) + 0.5, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
                break;
            }
        }
    }

    private static void spawnRune(Level level, BlockPos pos, float r, float g, float b, int duration, float gravity) {
        level.addParticle(new BlockRunesParticleOptions(r, g, b, duration, gravity, false), pos.getX() + 0.5, pos.getY() + 0.7F + 0.5, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
    }
}
