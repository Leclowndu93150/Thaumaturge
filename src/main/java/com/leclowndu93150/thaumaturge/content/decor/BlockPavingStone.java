package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
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
    public static final MapCodec<BlockPavingStone> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.fieldOf("barrier").forGetter(block -> block.barrier), propertiesCodec())
            .apply(instance, BlockPavingStone::new));

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    private static final int SPEED_DURATION = 40;
    private static final int SPEED_AMPLIFIER = 1;
    private static final double RUNE_CENTER = 0.5;
    private static final double RUNE_HEIGHT = 1.05;
    private static final RuneStyle POWERED_STYLE = new RuneStyle(0.3F, 0.4F, 0.2F, 0.25F, 0.85F, 0.15F, 2, 8, -0.06F);
    private static final RuneStyle ACTIVE_STYLE = new RuneStyle(0.8F, 0.2F, 0.2F, 0.3F, 0.6F, 0.4F, 4, 8, -0.06F);
    private static final RuneStyle WARNING_STYLE = new RuneStyle(0.85F, 0.15F, 0.0F, 0.2F, 0.0F, 0.15F, 1, 10, 0.0F);
    private static final double NEARBY_RANGE = 1.0;
    private static final double WARNING_MIN_SPREAD = 0.75;

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
            living.addEffect(
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SPEED_DURATION, SPEED_AMPLIFIER, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP, SPEED_DURATION, 0, false, false));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return barrier ? new BlockEntityBarrierStone(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!barrier || level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.BARRIER_STONE.get(), BlockEntityBarrierStone::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!barrier) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            spawnRunes(level, pos, random, POWERED_STYLE);
            return;
        }
        AABB nearby = new AABB(pos).inflate(NEARBY_RANGE);
        List<LivingEntity> intruders = level.getEntitiesOfClass(
                LivingEntity.class, nearby, candidate -> !(candidate instanceof Player) && candidate.isAlive());
        if (intruders.isEmpty()) {
            return;
        }
        BlockState barrierState = TTBlocks.BARRIER.get().defaultBlockState();
        if (level.getBlockState(pos.above(1)) == barrierState || level.getBlockState(pos.above(2)) == barrierState) {
            spawnRunes(level, pos, random, ACTIVE_STYLE);
            return;
        }
        LivingEntity intruder = intruders.get(random.nextInt(intruders.size()));
        double base = pos.getY() + RUNE_HEIGHT;
        double spread = Math.max(WARNING_MIN_SPREAD, intruder.getEyeY() - base);
        emitRune(
                level,
                pos.getX() + RUNE_CENTER,
                base + random.nextDouble() * spread,
                pos.getZ() + RUNE_CENTER,
                random,
                WARNING_STYLE);
    }

    private static void spawnRunes(Level level, BlockPos pos, RandomSource random, RuneStyle style) {
        for (int i = 0; i < style.count(); i++) {
            emitRune(
                    level, pos.getX() + RUNE_CENTER, pos.getY() + RUNE_HEIGHT, pos.getZ() + RUNE_CENTER, random, style);
        }
    }

    private static void emitRune(Level level, double x, double y, double z, RandomSource random, RuneStyle style) {
        float red = style.redBase() + random.nextFloat() * style.redSpread();
        float green = style.greenBase() + random.nextFloat() * style.greenSpread();
        float blue = style.blueBase() + random.nextFloat() * style.blueSpread();
        level.addParticle(
                new BlockRunesParticleOptions(red, green, blue, style.duration(), style.gravity(), false),
                x,
                y,
                z,
                0.0,
                0.0,
                0.0);
    }

    private record RuneStyle(
            float redBase,
            float redSpread,
            float greenBase,
            float greenSpread,
            float blueBase,
            float blueSpread,
            int count,
            int duration,
            float gravity) {}
}
