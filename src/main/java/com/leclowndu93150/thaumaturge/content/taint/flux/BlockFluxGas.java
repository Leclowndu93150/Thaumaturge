package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import com.leclowndu93150.thaumaturge.content.taint.FluxImmunityHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** TC4-style finite Flux Gas implemented as an upward-moving modern block. */
public final class BlockFluxGas extends Block {
    public static final MapCodec<BlockFluxGas> CODEC = simpleCodec(BlockFluxGas::new);
    public static final IntegerProperty AMOUNT = IntegerProperty.create("amount", 1, PhysicalFlux.MAX_QUANTA);

    private static final int TICK_DELAY = 10;
    private static final int CONTACT_EFFECT_CHANCE = 10;
    private static final int VIS_EXHAUST_DURATION = 1200;
    private static final int CONFUSION_BASE_DURATION = 80;
    private static final int CONFUSION_DURATION_PER_LEVEL = 20;
    private static final int REPLACEABLE_AMOUNT = 2;
    private static final int AMBIENT_FUME_CHANCE = 10;
    private static final int GAS_COLOR = ARGB32.color(0xFF, 0x9C, 0x1D, 0xB8);
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public BlockFluxGas(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AMOUNT, 1));
    }

    @Override
    public MapCodec<BlockFluxGas> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AMOUNT);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return state.getValue(AMOUNT) <= REPLACEABLE_AMOUNT;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !oldState.is(state.getBlock())) {
            scheduleTick(level, pos);
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if (level instanceof ServerLevel serverLevel) {
            scheduleTick(serverLevel, pos);
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(this)) {
            return;
        }
        int amount = state.getValue(AMOUNT);
        PhysicalFluxAuraContamination.observeGas(level, pos, amount);
        spread(level, pos, amount, random);
    }

    private void spread(ServerLevel level, BlockPos pos, int amount, RandomSource random) {
        int remaining = flowUp(level, pos, amount);
        if (remaining <= 0) {
            return;
        }

        BlockState current = level.getBlockState(pos);
        if (!current.is(this)) {
            return;
        }

        java.util.ArrayList<BlockPos> targets = new java.util.ArrayList<>(5);
        targets.add(pos);
        int total = remaining;
        for (Direction direction : HORIZONTAL) {
            BlockPos target = pos.relative(direction);
            int neighborAmount = amountAvailable(level, target);
            if (neighborAmount >= 0 && neighborAmount < remaining) {
                targets.add(target);
                total += neighborAmount;
            }
        }
        if (targets.size() == 1) {
            setAmount(level, pos, remaining);
            return;
        }

        // Shuffle the small candidate set so a remainder does not consistently prefer one world
        // direction. This keeps the gas diffuse without relying on liquid-flow assumptions.
        for (int i = targets.size() - 1; i > 0; i--) {
            int swap = random.nextInt(i + 1);
            BlockPos tmp = targets.get(i);
            targets.set(i, targets.get(swap));
            targets.set(swap, tmp);
        }

        int each = total / targets.size();
        int remainder = total % targets.size();
        for (int i = 0; i < targets.size(); i++) {
            setAmount(level, targets.get(i), each + (i < remainder ? 1 : 0));
        }
    }

    private int flowUp(ServerLevel level, BlockPos pos, int amount) {
        BlockPos above = pos.above();
        if (above.getY() >= level.getMaxBuildHeight()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return 0;
        }
        BlockState aboveState = level.getBlockState(above);
        if (!aboveState.getFluidState().isEmpty() && !PhysicalFlux.isPhysicalFlux(aboveState)) {
            level.setBlock(above, gasBlockState(amount), Block.UPDATE_ALL);
            level.setBlock(pos, aboveState, Block.UPDATE_ALL);
            FluidState displaced = aboveState.getFluidState();
            level.scheduleTick(pos, displaced.getType(), displaced.getType().getTickDelay(level));
            scheduleTick(level, above);
            return 0;
        }
        int aboveAmount = amountAvailable(level, above);
        if (aboveAmount < 0) {
            return amount;
        }

        int combined = amount + aboveAmount;
        int moved = Math.min(PhysicalFlux.MAX_QUANTA, combined);
        int remaining = combined - moved;
        setAmount(level, above, moved);
        setAmount(level, pos, remaining);
        return remaining;
    }

    private int amountAvailable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)) {
            return state.getValue(AMOUNT);
        }
        if (state.isAir()) {
            return 0;
        }
        if (PhysicalFlux.isPhysicalFlux(state)) {
            return -1;
        }
        if (!state.getFluidState().isEmpty()) return 0;
        return state.canBeReplaced() ? 0 : -1;
    }

    private void setAmount(ServerLevel level, BlockPos pos, int amount) {
        if (amount <= 0) {
            if (level.getBlockState(pos).is(this)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            return;
        }
        BlockState old = level.getBlockState(pos);
        int clamped = Math.min(PhysicalFlux.MAX_QUANTA, amount);
        if (old.is(this) && old.getValue(AMOUNT) == clamped) {
            return;
        }
        if (!old.isAir() && !old.is(this) && old.getFluidState().isEmpty() && old.canBeReplaced()) {
            level.destroyBlock(pos, false);
        }
        level.setBlock(pos, gasBlockState(clamped), Block.UPDATE_ALL);
        scheduleTick(level, pos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (living instanceof ITaintedMob
                || living.getType().is(EntityTypeTags.UNDEAD)
                || FluxImmunityHelper.isImmune(living)
                || living.hasEffect(TCMobEffects.VIS_EXHAUST)
                || living.hasEffect(MobEffects.CONFUSION)
                || serverLevel.getRandom().nextInt(CONTACT_EFFECT_CHANCE) != 0) {
            return;
        }

        int amount = state.getValue(AMOUNT);
        int meta = amount - 1;
        if (serverLevel.getRandom().nextBoolean()) {
            living.addEffect(
                    new MobEffectInstance(TCMobEffects.VIS_EXHAUST, VIS_EXHAUST_DURATION, meta / 3, true, true, false));
        } else {
            living.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION, CONFUSION_BASE_DURATION + meta * CONFUSION_DURATION_PER_LEVEL));
        }
        PhysicalFlux.reduce(serverLevel, pos, 1);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(AMBIENT_FUME_CHANCE) != 0) {
            return;
        }
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(new TaintFumeParticleOptions(GAS_COLOR, 0.65F), x, y, z, 0.0, 0.015, 0.0);
    }

    public static BlockState gasBlockState(int amount) {
        int clamped = Math.max(1, Math.min(PhysicalFlux.MAX_QUANTA, amount));
        return TCBlocks.FLUX_GAS.get().defaultBlockState().setValue(AMOUNT, clamped);
    }

    public static void scheduleTick(LevelAccessor level, BlockPos pos) {
        level.scheduleTick(pos, TCBlocks.FLUX_GAS.get(), TICK_DELAY);
    }
}
