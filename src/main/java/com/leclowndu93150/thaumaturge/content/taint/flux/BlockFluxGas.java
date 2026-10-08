package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import com.leclowndu93150.thaumaturge.content.taint.FluxImmunityHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockFluxGas extends Block implements PhysicalFluxBlock, LiquidBlockContainer {
    private static final int MIN_SPREAD_DIFFERENCE = 2;

    public static final MapCodec<BlockFluxGas> CODEC = simpleCodec(BlockFluxGas::new);
    public static final IntegerProperty AMOUNT = IntegerProperty.create("amount", 1, PhysicalFlux.MAX_QUANTA);

    private static final int TICK_DELAY = 10;
    private static final int SPREAD_TARGETS = 5;
    private static final int CONTACT_EFFECT_CHANCE = 10;
    private static final int VIS_EXHAUST_DURATION = 1200;
    private static final int LEVELS_PER_AMPLIFIER = 3;
    private static final int NAUSEA_BASE_DURATION = 80;
    private static final int NAUSEA_DURATION_PER_LEVEL = 20;
    private static final int REPLACEABLE_AMOUNT = 2;
    private static final int AMBIENT_FUME_CHANCE = 10;
    private static final int FUME_COLOR = ARGB32.color(0xFF, 0x9C, 0x1D, 0xB8);
    private static final float FUME_SCALE = 0.65F;
    private static final double FUME_RISE = 0.015;
    private static final float AURA_FLOOR_PER_QUANTUM = 0.25F;
    private static final float TAINT_WEIGHT_PER_QUANTUM = 0.35F;
    private static final int OUTBREAK_COST = 2;

    public BlockFluxGas(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AMOUNT, 1));
    }

    @Override
    protected MapCodec<BlockFluxGas> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AMOUNT);
    }

    public static BlockState gasBlockState(int amount) {
        return TTBlocks.FLUX_GAS
                .get()
                .defaultBlockState()
                .setValue(AMOUNT, Math.clamp(amount, 1, PhysicalFlux.MAX_QUANTA));
    }

    @Override
    public int fluxAmount(BlockState state) {
        return state.getValue(AMOUNT);
    }

    @Override
    public BlockState withFluxAmount(int amount) {
        return gasBlockState(amount);
    }

    @Override
    public void scheduleFluxTick(ServerLevel level, BlockPos pos) {
        level.scheduleTick(pos, this, TICK_DELAY);
    }

    @Override
    public float auraFloorPerQuantum() {
        return AURA_FLOOR_PER_QUANTUM;
    }

    @Override
    public float taintWeightPerQuantum() {
        return TAINT_WEIGHT_PER_QUANTUM;
    }

    @Override
    public int outbreakCost() {
        return OUTBREAK_COST;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return state.getValue(AMOUNT) <= REPLACEABLE_AMOUNT;
    }

    @Override
    public boolean canPlaceLiquid(
            @Nullable Player user, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        PhysicalFluxAuraFloor.observe(serverLevel, pos);
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, TICK_DELAY);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        PhysicalFluxAuraFloor.observe(level, pos);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction directionToNeighbour,
            BlockState neighbourState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighbourPos) {
        level.scheduleTick(pos, this, TICK_DELAY);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int remaining = flowUp(level, pos, state.getValue(AMOUNT));
        if (remaining > 0 && level.getBlockState(pos).is(this)) {
            spreadSideways(level, pos, remaining, random);
        }
    }

    private int flowUp(ServerLevel level, BlockPos pos, int amount) {
        BlockPos above = pos.above();
        if (above.getY() > (level.getMaxBuildHeight() - 1)) {
            setAmount(level, pos, 0);
            return 0;
        }
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getBlock() instanceof LiquidBlock && !PhysicalFlux.isPhysicalFlux(aboveState)) {
            level.setBlock(above, gasBlockState(amount), Block.UPDATE_ALL);
            level.setBlock(pos, aboveState, Block.UPDATE_ALL);
            FluidState displaced = aboveState.getFluidState();
            level.scheduleTick(pos, displaced.getType(), displaced.getType().getTickDelay(level));
            level.scheduleTick(above, this, TICK_DELAY);
            return 0;
        }
        int aboveAmount = room(level, above);
        if (aboveAmount < 0 || aboveAmount >= PhysicalFlux.MAX_QUANTA) {
            return amount;
        }
        int combined = amount + aboveAmount;
        int moved = Math.min(PhysicalFlux.MAX_QUANTA, combined);
        setAmount(level, above, moved);
        setAmount(level, pos, combined - moved);
        return combined - moved;
    }

    private void spreadSideways(ServerLevel level, BlockPos pos, int amount, RandomSource random) {
        BlockPos[] targets = new BlockPos[SPREAD_TARGETS];
        targets[0] = pos;
        int count = 1;
        int total = amount;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos target = pos.relative(direction);
            int neighbour = room(level, target);
            if (neighbour >= 0 && amount - neighbour >= MIN_SPREAD_DIFFERENCE) {
                targets[count++] = target;
                total += neighbour;
            }
        }
        if (count == 1) {
            return;
        }
        for (int i = count - 1; i > 0; i--) {
            int swap = random.nextInt(i + 1);
            BlockPos held = targets[i];
            targets[i] = targets[swap];
            targets[swap] = held;
        }
        int each = total / count;
        int remainder = total % count;
        for (int i = 0; i < count; i++) {
            setAmount(level, targets[i], each + (i < remainder ? 1 : 0));
        }
    }

    private int room(ServerLevel level, BlockPos pos) {
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
        if (!state.getFluidState().isEmpty()) {
            return state.getBlock() instanceof LiquidBlock ? 0 : -1;
        }
        return state.canBeReplaced() ? 0 : -1;
    }

    private void setAmount(ServerLevel level, BlockPos pos, int amount) {
        BlockState old = level.getBlockState(pos);
        if (amount <= 0) {
            if (old.is(this)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            return;
        }
        int clamped = Math.min(PhysicalFlux.MAX_QUANTA, amount);
        if (old.is(this) && old.getValue(AMOUNT) == clamped) {
            return;
        }
        if (!old.isAir() && !old.is(this)) {
            level.destroyBlock(pos, false);
        }
        level.setBlock(pos, gasBlockState(clamped), Block.UPDATE_ALL);
        level.scheduleTick(pos, this, TICK_DELAY);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(entity instanceof LivingEntity living)
                || resists(living)
                || serverLevel.getRandom().nextInt(CONTACT_EFFECT_CHANCE) != 0) {
            return;
        }
        int thickness = state.getValue(AMOUNT) - 1;
        if (serverLevel.getRandom().nextBoolean()) {
            living.addEffect(new MobEffectInstance(
                    TTMobEffects.VIS_EXHAUST,
                    VIS_EXHAUST_DURATION,
                    thickness / LEVELS_PER_AMPLIFIER,
                    true,
                    true,
                    false));
        } else {
            living.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION, NAUSEA_BASE_DURATION + thickness * NAUSEA_DURATION_PER_LEVEL));
        }
        PhysicalFlux.reduce(serverLevel, pos, 1);
    }

    private static boolean resists(LivingEntity living) {
        return MobTraits.isTainted(living)
                || living.getType().is(EntityTypeTags.UNDEAD)
                || FluxImmunityHelper.isImmune(living)
                || living.hasEffect(TTMobEffects.VIS_EXHAUST)
                || living.hasEffect(MobEffects.CONFUSION);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(AMBIENT_FUME_CHANCE) == 0) {
            level.addParticle(
                    new TaintFumeParticleOptions(FUME_COLOR, FUME_SCALE),
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0,
                    FUME_RISE,
                    0.0);
        }
    }
}
