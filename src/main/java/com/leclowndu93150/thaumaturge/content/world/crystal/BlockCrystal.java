package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockCrystal extends Block {
    private static final int MAX_SIZE = 3;
    private static final int MAX_GENERATION = 4;

    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, MAX_SIZE);
    public static final IntegerProperty GENERATION = IntegerProperty.create("gen", 1, MAX_GENERATION);

    private static final int TICK_CHANCE_BASE = 3;
    private static final int SPREAD_ONE_IN = 16;
    private static final int KEEP_GENERATION_ONE_IN = 6;
    private static final int SPREAD_REACH = 1;
    private static final int SPREAD_SPAN = SPREAD_REACH * 2 + 1;
    private static final int CEILING_BASE = 5;
    private static final long CEILING_VARIANTS = 3L;
    private static final float AURA_BAND = 10.0F;
    private static final float AURA_EXCHANGE = 10.0F;
    private static final float DECAY_FLUX = 1.0F;
    private static final float FLUX_CONVERSION_DIVISOR = 2.0F;
    private static final float DRAIN_TOLERANCE = 0.001F;
    private final ResourceKey<IAspect> aspect;
    private final boolean flux;

    public BlockCrystal(BlockBehaviour.Properties properties, ResourceKey<IAspect> aspect, boolean flux) {
        super(properties);
        this.aspect = aspect;
        this.flux = flux;
        registerDefaultState(stateDefinition.any().setValue(SIZE, 0).setValue(GENERATION, 1));
    }

    private static final MapCodec<BlockCrystal> CODEC = simpleCodec(p -> new BlockCrystal(p, null, false));

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public ResourceKey<IAspect> aspect() {
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
        return CrystalShapes.shapeFor(level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
        return hasSturdyNeighbour(level, pos);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbourState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighbourPos) {
        if (!state.canSurvive(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
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
        switch (bandFor(stock, base)) {
            case STARVED -> wither(state, level, pos);
            case RICH -> flourish(state, level, pos, random);
            case MIDDLE -> {
                if (!flux) {
                    convertToFlux(state, level, pos, stock, base);
                }
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
            level.setBlock(pos, state.setValue(SIZE, size - 1), 3);
            returnToAura(level, pos);
            return;
        }
        if (!hasSameCrystalBeside(level, pos)) {
            return;
        }
        level.removeBlock(pos, false);
        returnToAura(level, pos);
        if (!flux) {
            AuraHelper.addFlux(level, pos, DECAY_FLUX);
        }
    }

    private void flourish(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int size = growth(state);
        if (size < sizeCeiling(state, pos)) {
            if (drain(level, pos, AURA_EXCHANGE) > 0.0F) {
                level.setBlock(pos, state.setValue(SIZE, size + 1), 3);
            }
            return;
        }
        if (generation(state) < MAX_GENERATION) {
            seed(state, level, pos, random);
        }
    }

    static int sizeCeiling(BlockState state, BlockPos pos) {
        int growthBonus = (int) Math.floorMod(pos.asLong(), CEILING_VARIANTS);
        return Math.min(MAX_SIZE, CEILING_BASE - state.getValue(GENERATION) + growthBonus);
    }

    private void seed(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos target = neighbourWithin(pos, random);
        if (target.equals(pos)
                || !isOpenCell(level, target)
                || random.nextInt(SPREAD_ONE_IN) != 0
                || !hasSturdyNeighbour(level, target)) {
            return;
        }
        if (drain(level, pos, AURA_EXCHANGE) <= 0.0F) {
            return;
        }
        int parentGeneration = generation(state);
        int childGeneration = childGeneration(parentGeneration, random);
        level.setBlock(target, defaultBlockState().setValue(SIZE, 0).setValue(GENERATION, childGeneration), 3);
    }

    static BlockPos neighbourWithin(BlockPos pos, RandomSource random) {
        int dx = random.nextInt(SPREAD_SPAN) - SPREAD_REACH;
        int dy = random.nextInt(SPREAD_SPAN) - SPREAD_REACH;
        int dz = random.nextInt(SPREAD_SPAN) - SPREAD_REACH;
        return pos.offset(dx, dy, dz);
    }

    static int childGeneration(int parentGeneration, RandomSource random) {
        return random.nextInt(KEEP_GENERATION_ONE_IN) == 0
                ? parentGeneration
                : Math.min(parentGeneration + 1, MAX_GENERATION);
    }

    private static boolean isOpenCell(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty() && (state.isAir() || state.canBeReplaced());
    }

    private boolean hasSameCrystalBeside(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(this)) {
                return true;
            }
        }
        return false;
    }

    private float currentStock(ServerLevel level, BlockPos pos) {
        return flux ? AuraHelper.getFlux(level, pos) : AuraHelper.getVis(level, pos);
    }

    private void convertToFlux(BlockState state, ServerLevel level, BlockPos pos, float vis, float base) {
        float fluxStock = AuraHelper.getFlux(level, pos);
        if (fluxStock <= vis || fluxStock <= base / FLUX_CONVERSION_DIVISOR) {
            return;
        }
        float cost = growth(state) + 1;
        if (AuraHelper.drainFlux(level, pos, cost, false) < cost - DRAIN_TOLERANCE) {
            return;
        }
        BlockState tainted = TTBlocks.CRYSTAL_VITIUM
                .get()
                .defaultBlockState()
                .setValue(SIZE, growth(state))
                .setValue(GENERATION, generation(state));
        level.setBlock(pos, tainted, 3);
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

    private static boolean hasSturdyNeighbour(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            if (level.getBlockState(neighbour).isFaceSturdy(level, neighbour, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    private enum AuraBand {
        STARVED,
        RICH,
        MIDDLE
    }
}
