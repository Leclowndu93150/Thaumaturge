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
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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

public final class BlockFluxGas extends Block implements PhysicalFluxBlock, LiquidBlockContainer {
    public static final MapCodec<BlockFluxGas> CODEC = simpleCodec(BlockFluxGas::new);
    public static final IntegerProperty AMOUNT = IntegerProperty.create("amount", 1, PhysicalFlux.MAX_QUANTA);

    private static final int REPLACEABLE_AMOUNT = 2;
    private static final int TICK_DELAY = 10;
    private static final float AURA_FLOOR_PER_QUANTUM = 0.25F;
    private static final float TAINT_WEIGHT_PER_QUANTUM = 0.35F;
    private static final int OUTBREAK_COST = 2;
    private static final int MIN_SPREAD_GAP = 2;
    private static final int SPREAD_CELLS = 5;
    private static final int CONTACT_ONE_IN = 10;
    private static final int VIS_EXHAUST_TICKS = 1200;
    private static final int VIS_EXHAUST_QUANTA_PER_LEVEL = 3;
    private static final int VIS_EXHAUST_MAX_AMPLIFIER = 2;
    private static final int NAUSEA_BASE_SECONDS = 3;
    private static final int TICKS_PER_SECOND = 20;
    private static final int FUME_ONE_IN = 10;
    private static final int FUME_COLOR = 0xFF9C1DB8;
    private static final float FUME_SCALE = 0.65F;
    private static final double FUME_RISE = 0.015;

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
        return TTBlocks.FLUX_GAS.get().defaultBlockState().setValue(AMOUNT, Math.clamp(amount, 1, PhysicalFlux.MAX_QUANTA));
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
    protected boolean skipRendering(BlockState state, BlockState neighbour, Direction direction) {
        return neighbour.is(this);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return fluxAmount(state) <= REPLACEABLE_AMOUNT;
    }

    @Override
    public boolean canPlaceLiquid(LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel server) {
            PhysicalFluxAuraFloor.observe(server, pos);
            if (!oldState.is(this)) {
                scheduleFluxTick(server, pos);
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        PhysicalFluxAuraFloor.observe(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        ticks.scheduleTick(pos, this, TICK_DELAY);
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int remaining = rise(level, pos, fluxAmount(state));
        if (remaining > 0) {
            spread(level, pos, remaining, random);
        }
    }

    private static boolean isOrdinaryLiquid(BlockState state) {
        return state.getBlock() instanceof LiquidBlock && !PhysicalFlux.isPhysicalFlux(state);
    }

    private static int room(BlockState target) {
        if (target.is(TTBlocks.FLUX_GAS)) {
            return PhysicalFlux.MAX_QUANTA - target.getValue(AMOUNT);
        }
        if (target.isAir()) {
            return PhysicalFlux.MAX_QUANTA;
        }
        if (PhysicalFlux.isPhysicalFlux(target)) {
            return 0;
        }
        if (!target.getFluidState().isEmpty()) {
            return 0;
        }
        return target.canBeReplaced() ? PhysicalFlux.MAX_QUANTA : 0;
    }

    private static int held(BlockState target) {
        return target.is(TTBlocks.FLUX_GAS) ? target.getValue(AMOUNT) : 0;
    }

    private void store(ServerLevel level, BlockPos pos, int amount) {
        level.setBlock(pos, amount > 0 ? gasBlockState(amount) : Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (amount > 0) {
            scheduleFluxTick(level, pos);
        }
    }

    private int rise(ServerLevel level, BlockPos pos, int amount) {
        BlockPos above = pos.above();
        if (level.isOutsideBuildHeight(above)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return 0;
        }
        BlockState aboveState = level.getBlockState(above);
        if (isOrdinaryLiquid(aboveState)) {
            level.setBlock(above, gasBlockState(amount), Block.UPDATE_ALL);
            level.setBlock(pos, aboveState, Block.UPDATE_ALL);
            scheduleFluxTick(level, above);
            return 0;
        }
        int moved = Math.min(amount, room(aboveState));
        if (moved <= 0) {
            return amount;
        }
        store(level, above, held(aboveState) + moved);
        int remaining = amount - moved;
        store(level, pos, remaining);
        return remaining;
    }

    private void spread(ServerLevel level, BlockPos pos, int amount, RandomSource random) {
        BlockPos[] cells = new BlockPos[SPREAD_CELLS];
        int[] levels = new int[SPREAD_CELLS];
        cells[0] = pos;
        levels[0] = amount;
        int count = 1;
        int total = amount;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(direction);
            if (!level.hasChunkAt(neighbour)) {
                continue;
            }
            BlockState target = level.getBlockState(neighbour);
            int content = held(target);
            if (room(target) > 0 && content <= amount - MIN_SPREAD_GAP) {
                cells[count] = neighbour;
                levels[count] = content;
                total += content;
                count++;
            }
        }
        if (count == 1) {
            return;
        }
        int share = total / count;
        int leftover = total % count;
        boolean[] bonus = new boolean[count];
        while (leftover > 0) {
            int index = random.nextInt(count);
            if (!bonus[index]) {
                bonus[index] = true;
                leftover--;
            }
        }
        for (int index = 0; index < count; index++) {
            int result = share + (bonus[index] ? 1 : 0);
            if (result != levels[index]) {
                store(level, cells[index], result);
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof LivingEntity living) || !isContactCell(level, pos, living) || isShielded(living)) {
            return;
        }
        RandomSource random = server.getRandom();
        if (random.nextInt(CONTACT_ONE_IN) != 0) {
            return;
        }
        int amount = fluxAmount(state);
        living.addEffect(random.nextBoolean() ? visExhaust(amount) : nausea(amount));
        PhysicalFlux.reduce(server, pos, 1);
    }

    private static boolean isContactCell(Level level, BlockPos pos, LivingEntity living) {
        BlockPos feet = living.blockPosition();
        if (pos.equals(feet)) {
            return true;
        }
        return !level.getBlockState(feet).is(TTBlocks.FLUX_GAS) && pos.equals(BlockPos.containing(living.getEyePosition()));
    }

    private static boolean isShielded(LivingEntity living) {
        return MobTraits.isTainted(living) || living.is(EntityTypeTags.UNDEAD) || FluxImmunityHelper.isImmune(living) || living.hasEffect(TTMobEffects.VIS_EXHAUST)
                || living.hasEffect(MobEffects.NAUSEA);
    }

    private static MobEffectInstance visExhaust(int amount) {
        int amplifier = Math.min(VIS_EXHAUST_MAX_AMPLIFIER, (amount - 1) / VIS_EXHAUST_QUANTA_PER_LEVEL);
        return new MobEffectInstance(TTMobEffects.VIS_EXHAUST, VIS_EXHAUST_TICKS, amplifier, false, true, false);
    }

    private static MobEffectInstance nausea(int amount) {
        return new MobEffectInstance(MobEffects.NAUSEA, (NAUSEA_BASE_SECONDS + amount) * TICKS_PER_SECOND);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(FUME_ONE_IN) == 0) {
            level.addParticle(new TaintFumeParticleOptions(FUME_COLOR, FUME_SCALE), pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0.0,
                    FUME_RISE, 0.0);
        }
    }
}
