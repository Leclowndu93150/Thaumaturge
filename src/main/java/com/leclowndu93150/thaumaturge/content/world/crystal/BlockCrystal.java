package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockCrystal extends Block {
    private static final int MAX_SIZE = 3;
    private static final int MAX_GENERATION = 4;
    private static final int MIN_GENERATION = 1;

    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, MAX_SIZE);
    public static final IntegerProperty GENERATION = IntegerProperty.create("gen", MIN_GENERATION, MAX_GENERATION);

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final MapCodec<BlockCrystal> CODEC = simpleCodec(properties -> new BlockCrystal(properties, null, false));
    private static final int MAX_SPAWN_SIZE = 0;
    private static final int SUPPORT_RECHECK_DELAY = 1;
    private static final int CHANGE_FLAGS = 3;
    private static final int SPREAD_REACH = 1;
    private static final int SPREAD_SPAN = 2 * SPREAD_REACH + 1;
    private static final float DECAY_FLUX = 1.0F;
    private static final float DRAIN_TOLERANCE = 0.001F;
    private static final float FLUX_CONVERSION_DIVISOR = 2.0F;
    private static final int TICK_CHANCE_BASE = 3;
    private static final float AURA_BAND = 10.0F;
    private static final float AURA_EXCHANGE = 10.0F;
    private static final int CEILING_BASE = 5;
    private static final int CEILING_BONUS_VARIANTS = 3;
    private static final int SPREAD_ONE_IN = 16;
    private static final int KEEP_GENERATION_ONE_IN = 6;

    private final @Nullable ResourceKey<IAspect> aspect;
    private final boolean flux;

    public BlockCrystal(Properties properties, @Nullable ResourceKey<IAspect> aspect, boolean flux) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(GENERATION, MIN_GENERATION).setValue(SIZE, 0));
        this.flux = flux;
        this.aspect = aspect;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public @Nullable ResourceKey<IAspect> aspect() {
        return aspect;
    }

    public boolean isFlux() {
        return flux;
    }

    public int growth(BlockState state) {
        return state.getValue(SIZE);
    }

    public int generation(BlockState state) {
        return state.getValue(GENERATION);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE, GENERATION);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CrystalShapes.shapeFor(state, level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSupport(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        boolean unsupported = !hasSupport(level, pos);
        if (unsupported && !level.isClientSide()) {
            ticks.scheduleTick(pos, this, SUPPORT_RECHECK_DELAY);
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!hasSupport(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(TICK_CHANCE_BASE + generation(state)) != 0) {
            return;
        }
        float stock = currentStock(level, pos);
        float base = AuraHelper.getAuraBase(level, pos);
        if (!flux) {
            convertToFlux(state, level, pos, stock, base);
            if (!level.getBlockState(pos).is(this)) {
                return;
            }
        }
        switch (bandFor(stock, base)) {
            case STARVED -> wither(state, level, pos);
            case RICH -> flourish(state, level, pos, random);
            case MIDDLE -> {
            }
        }
    }

    private static AuraBand bandFor(float stock, float base) {
        if (stock <= AURA_BAND) {
            return AuraBand.STARVED;
        }
        return stock > base + AURA_BAND ? AuraBand.RICH : AuraBand.MIDDLE;
    }

    private void wither(BlockState state, ServerLevel level, BlockPos pos) {
        int size = growth(state);
        if (size > 0) {
            level.setBlock(pos, state.setValue(SIZE, size - 1), CHANGE_FLAGS);
        } else if (hasSameCrystalBeside(level, pos)) {
            level.removeBlock(pos, false);
        } else {
            return;
        }
        returnToAura(level, pos);
        if (!flux) {
            AuraHelper.addFlux(level, pos, DECAY_FLUX);
        }
    }

    private void flourish(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int size = growth(state);
        if (size < sizeCeiling(state, pos)) {
            if (drain(level, pos, AURA_EXCHANGE) > 0.0F) {
                level.setBlock(pos, state.setValue(SIZE, size + 1), CHANGE_FLAGS);
            }
            return;
        }
        if (generation(state) < MAX_GENERATION) {
            seed(state, level, pos, random);
        }
    }

    private static int sizeCeiling(BlockState state, BlockPos pos) {
        int bonus = Math.floorMod(Mth.getSeed(pos), CEILING_BONUS_VARIANTS);
        return Math.min(MAX_SIZE, CEILING_BASE - state.getValue(GENERATION) + bonus);
    }

    private void seed(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos target = neighbourWithin(pos, random);
        if (random.nextInt(SPREAD_ONE_IN) != 0 || !canHost(level, target)) {
            return;
        }
        if (drain(level, pos, AURA_EXCHANGE) <= 0.0F) {
            return;
        }
        int parent = generation(state);
        int child = random.nextInt(KEEP_GENERATION_ONE_IN) == 0 ? parent : Math.min(parent + 1, MAX_GENERATION);
        level.setBlock(target, defaultBlockState().setValue(SIZE, MAX_SPAWN_SIZE).setValue(GENERATION, child), CHANGE_FLAGS);
    }

    private static BlockPos neighbourWithin(BlockPos pos, RandomSource random) {
        int cells = SPREAD_SPAN * SPREAD_SPAN * SPREAD_SPAN;
        int centre = cells / 2;
        int pick = random.nextInt(cells - 1);
        int cell = pick >= centre ? pick + 1 : pick;
        int dx = cell % SPREAD_SPAN - SPREAD_REACH;
        int dy = cell / SPREAD_SPAN % SPREAD_SPAN - SPREAD_REACH;
        int dz = cell / (SPREAD_SPAN * SPREAD_SPAN) - SPREAD_REACH;
        return pos.offset(dx, dy, dz);
    }

    private static boolean canHost(ServerLevel level, BlockPos pos) {
        BlockState existing = level.getBlockState(pos);
        return existing.getFluidState().isEmpty() && (existing.isAir() || existing.canBeReplaced()) && hasSupport(level, pos);
    }

    private boolean hasSameCrystalBeside(ServerLevel level, BlockPos pos) {
        return Arrays.stream(DIRECTIONS).anyMatch(side -> level.getBlockState(pos.relative(side)).is(this));
    }

    private float currentStock(ServerLevel level, BlockPos pos) {
        return flux ? AuraHelper.getFlux(level, pos) : AuraHelper.getVis(level, pos);
    }

    private static boolean hasSupport(LevelReader level, BlockPos pos) {
        return Arrays.stream(DIRECTIONS).anyMatch(side -> CrystalShards.supports(level, pos, side));
    }

    private void convertToFlux(BlockState state, ServerLevel level, BlockPos pos, float vis, float base) {
        float fluxStock = AuraHelper.getFlux(level, pos);
        boolean fluxDominates = fluxStock > vis && fluxStock > base / FLUX_CONVERSION_DIVISOR;
        if (!fluxDominates) {
            return;
        }
        float cost = growth(state) + 1;
        float drained = AuraHelper.drainFlux(level, pos, cost, false);
        if (drained < cost - DRAIN_TOLERANCE) {
            return;
        }
        BlockState converted = TTBlocks.CRYSTAL_VITIUM.get().defaultBlockState().setValue(SIZE, growth(state)).setValue(GENERATION, generation(state));
        level.setBlock(pos, converted, CHANGE_FLAGS);
    }

    private float drain(ServerLevel level, BlockPos pos, float amount) {
        return flux ? AuraHelper.drainFlux(level, pos, amount, false) : AuraHelper.drainVis(level, pos, amount, false);
    }

    private void returnToAura(ServerLevel level, BlockPos pos) {
        if (flux) {
            AuraHelper.addFlux(level, pos, AURA_EXCHANGE);
        } else {
            AuraHelper.addVis(level, pos, AURA_EXCHANGE);
        }
    }

    private enum AuraBand {
        STARVED, RICH, MIDDLE
    }
}
