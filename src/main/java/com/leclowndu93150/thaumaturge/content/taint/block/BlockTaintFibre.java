package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.effect.FluxTaintExposure;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockTaintFibre extends Block implements ITaintBlock {
    public static final MapCodec<BlockTaintFibre> CODEC = simpleCodec(BlockTaintFibre::new);

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty GROWTH1 = BooleanProperty.create("growth1");
    public static final BooleanProperty GROWTH2 = BooleanProperty.create("growth2");
    public static final BooleanProperty GROWTH3 = BooleanProperty.create("growth3");
    public static final BooleanProperty GROWTH4 = BooleanProperty.create("growth4");

    private static final BooleanProperty TUFT = GROWTH1;
    private static final BooleanProperty STALK = GROWTH2;
    private static final BooleanProperty BULB = GROWTH3;
    private static final BooleanProperty HANGING = GROWTH4;
    private static final BooleanProperty[] GROWTHS = {TUFT, STALK, BULB, HANGING};

    private static final int GROWTH_ROLL_RANGE = 100;
    private static final int TUFT_PERCENT = 8;
    private static final int STALK_PERCENT = 4;
    private static final int BULB_PERCENT = 2;
    private static final int HANGING_PERCENT = 4;
    private static final int TUFT_UNDER = TUFT_PERCENT;
    private static final int STALK_UNDER = TUFT_UNDER + STALK_PERCENT;
    private static final int BULB_UNDER = STALK_UNDER + BULB_PERCENT;
    private static final int HANGING_UNDER = BULB_UNDER + HANGING_PERCENT;

    private static final double SHEET = 1.0;
    private static final double FULL = 16.0;
    private static final VoxelShape TUFT_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 7.0, 14.0);
    private static final VoxelShape STALK_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);
    private static final VoxelShape BULB_SHAPE = Shapes.or(Block.box(4.0, 1.0, 5.0, 12.0, 4.0, 11.0), Block.box(5.0, 0.0, 5.0, 11.0, 5.0, 11.0), Block.box(5.0, 1.0, 4.0, 11.0, 4.0, 12.0),
            Block.box(6.0, 5.0, 7.0, 8.0, 6.0, 9.0));
    private static final VoxelShape HANGING_SHAPE = Block.box(2.0, 3.0, 2.0, 14.0, 16.0, 14.0);
    private static final Map<Direction, VoxelShape> SHEET_SHAPES = sheetShapes();

    private static final int CONTACT_ONE_IN = 750;
    private static final float RIPEN_PRESSURE = 0.02F;
    private static final int BULB_MIN_FLUX = 3;
    private static final int BULB_FLUX_SPREAD = 3;

    private final Function<BlockState, VoxelShape> shapes;

    public BlockTaintFibre(Properties properties) {
        super(properties);
        BlockState base = stateDefinition.any();
        for (Direction direction : Direction.values()) {
            base = base.setValue(propertyFor(direction), false);
        }
        for (BooleanProperty growth : GROWTHS) {
            base = base.setValue(growth, false);
        }
        registerDefaultState(base);
        this.shapes = getShapeForEachState(BlockTaintFibre::outline);
    }

    @Override
    public MapCodec<BlockTaintFibre> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, GROWTH1, GROWTH2, GROWTH3, GROWTH4);
    }

    public static BooleanProperty propertyFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    public static BlockState stateForWorld(LevelReader level, BlockPos pos) {
        BlockState state = TTBlocks.TAINT_FIBRE.get().defaultBlockState();
        for (Direction direction : Direction.values()) {
            state = state.setValue(propertyFor(direction), TaintHelper.facesSturdily(level, pos, direction));
        }
        BooleanProperty growth = growthAt(pos, state.getValue(DOWN), state.getValue(UP));
        return growth == null ? state : state.setValue(growth, true);
    }

    public static boolean hasSolidAttachment(LevelReader level, BlockPos pos) {
        return TaintHelper.hasSturdyNeighbour(level, pos);
    }

    public static boolean isOnlyAdjacentToTaint(LevelAccessor level, BlockPos pos) {
        return TaintHelper.isRootless(level, pos);
    }

    public static boolean isHemmedByTaint(LevelAccessor level, BlockPos pos) {
        int open = 0;
        for (Direction direction : Direction.values()) {
            if (!TaintHelper.isTaintBlock(level.getBlockState(pos.relative(direction))) && !TaintHelper.facesSturdily(level, pos, direction)) {
                open++;
            }
        }
        return TaintHelper.countAdjacentTaint(level, pos) > open;
    }

    public static boolean hasGrowth(BlockState state) {
        for (BooleanProperty growth : GROWTHS) {
            if (state.getValue(growth)) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable BooleanProperty growthAt(BlockPos pos, boolean floor, boolean ceiling) {
        int roll = Math.floorMod(Mth.getSeed(pos), GROWTH_ROLL_RANGE);
        if (roll < BULB_UNDER) {
            if (!floor) {
                return null;
            }
            return roll < TUFT_UNDER ? TUFT : roll < STALK_UNDER ? STALK : BULB;
        }
        return roll < HANGING_UNDER && ceiling ? HANGING : null;
    }

    private static boolean hasSheet(BlockState state) {
        for (Direction direction : Direction.values()) {
            if (state.getValue(propertyFor(direction))) {
                return true;
            }
        }
        return false;
    }

    private static Map<Direction, VoxelShape> sheetShapes() {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        map.put(Direction.DOWN, Block.box(0.0, 0.0, 0.0, FULL, SHEET, FULL));
        map.put(Direction.UP, Block.box(0.0, FULL - SHEET, 0.0, FULL, FULL, FULL));
        map.put(Direction.NORTH, Block.box(0.0, 0.0, 0.0, FULL, FULL, SHEET));
        map.put(Direction.SOUTH, Block.box(0.0, 0.0, FULL - SHEET, FULL, FULL, FULL));
        map.put(Direction.WEST, Block.box(0.0, 0.0, 0.0, SHEET, FULL, FULL));
        map.put(Direction.EAST, Block.box(FULL - SHEET, 0.0, 0.0, FULL, FULL, FULL));
        return map;
    }

    private static VoxelShape outline(BlockState state) {
        VoxelShape shape = Shapes.empty();
        for (Direction direction : Direction.values()) {
            if (state.getValue(propertyFor(direction))) {
                shape = Shapes.or(shape, SHEET_SHAPES.get(direction));
            }
        }
        if (state.getValue(TUFT)) {
            shape = Shapes.or(shape, TUFT_SHAPE);
        }
        if (state.getValue(STALK)) {
            shape = Shapes.or(shape, STALK_SHAPE);
        }
        if (state.getValue(BULB)) {
            shape = Shapes.or(shape, BULB_SHAPE);
        }
        if (state.getValue(HANGING)) {
            shape = Shapes.or(shape, HANGING_SHAPE);
        }
        return shape;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.apply(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return stateForWorld(context.getLevel(), context.getClickedPos());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSolidAttachment(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockState reshaped = stateForWorld(level, pos);
        return hasSheet(reshaped) ? reshaped : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.trySpreadTaintedBiome(level, pos, random);
        if (withers(state, level, pos)) {
            decay(level, pos, state);
            return;
        }
        if (state.getValue(BULB) && tryRipen(level, pos)) {
            return;
        }
        TaintHelper.attemptFibreGrowth(level, pos, false);
    }

    private static boolean withers(BlockState state, ServerLevel level, BlockPos pos) {
        if (!hasGrowth(state) && TaintHelper.isRootless(level, pos)) {
            return true;
        }
        return !TaintHelper.isEcologicallySustained(level, pos);
    }

    private static boolean tryRipen(ServerLevel level, BlockPos pos) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || TaintBlooms.isProtected(level, pos) || !TaintEcology.isTainted(level, pos)) {
            return false;
        }
        BlockState stalk = TTBlocks.TAINT_SPORE_STALK.get().defaultBlockState();
        if (!stalk.canSurvive(level, pos)) {
            return false;
        }
        level.setBlock(pos, stalk, Block.UPDATE_ALL);
        TaintEcology.addPressure(level, pos, RIPEN_PRESSURE);
        return true;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && state.getValue(BULB)) {
            AuraHelper.polluteAura(level, pos, BULB_MIN_FLUX + level.getRandom().nextInt(BULB_FLUX_SPREAD), true);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (pos.equals(entity.blockPosition())) {
            FluxTaintExposure.onStep(level, entity, CONTACT_ONE_IN);
        }
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }
}
