package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

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

    private static final Map<Direction, BooleanProperty> FACE_BY_DIRECTION = createFaceMap();

    private static final VoxelShape SHAPE_UP = Shapes.box(0.0, 0.95, 0.0, 1.0, 1.0, 1.0);
    private static final VoxelShape SHAPE_DOWN = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.05, 1.0);
    private static final VoxelShape SHAPE_EAST = Shapes.box(0.95, 0.0, 0.0, 1.0, 1.0, 1.0);
    private static final VoxelShape SHAPE_WEST = Shapes.box(0.0, 0.0, 0.0, 0.05, 1.0, 1.0);
    private static final VoxelShape SHAPE_SOUTH = Shapes.box(0.0, 0.0, 0.95, 1.0, 1.0, 1.0);
    private static final VoxelShape SHAPE_NORTH = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 0.05);

    private static final VoxelShape SHAPE_GROWTH1 = Shapes.box(0.1, 0.0, 0.1, 0.9, 0.4, 0.9);
    private static final VoxelShape SHAPE_GROWTH2 = Shapes.box(0.2, 0.0, 0.2, 0.8, 1.0, 0.8);
    private static final VoxelShape SHAPE_GROWTH3 = Shapes.box(0.25, 0.0, 0.25, 0.75, 0.3125, 0.75);
    private static final VoxelShape SHAPE_GROWTH4 = Shapes.box(0.1, 0.3, 0.1, 0.9, 1.0, 0.9);

    private static final int GROWTH_RNG_RANGE = 50;
    private static final int GROWTH1_THRESHOLD = 4;
    private static final int GROWTH2_THRESHOLD_LO = 4;
    private static final int GROWTH2_THRESHOLD_HI = 5;
    private static final int GROWTH3_VALUE = 6;
    private static final int GROWTH4_THRESHOLD = 47;

    private static final int WALK_EFFECT_CHANCE = 750;
    private static final int WALK_EFFECT_DURATION = 200;

    public BlockTaintFibre(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false)
                .setValue(GROWTH1, false).setValue(GROWTH2, false).setValue(GROWTH3, false).setValue(GROWTH4, false));
    }

    private static Map<Direction, BooleanProperty> createFaceMap() {
        Map<Direction, BooleanProperty> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, NORTH);
        map.put(Direction.EAST, EAST);
        map.put(Direction.SOUTH, SOUTH);
        map.put(Direction.WEST, WEST);
        map.put(Direction.UP, UP);
        map.put(Direction.DOWN, DOWN);
        return map;
    }

    public static BooleanProperty propertyFor(Direction direction) {
        return FACE_BY_DIRECTION.get(direction);
    }

    @Override
    public MapCodec<BlockTaintFibre> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, GROWTH1, GROWTH2, GROWTH3, GROWTH4);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        if (state.getValue(UP))
            shape = Shapes.or(shape, SHAPE_UP);
        if (state.getValue(DOWN))
            shape = Shapes.or(shape, SHAPE_DOWN);
        if (state.getValue(NORTH))
            shape = Shapes.or(shape, SHAPE_NORTH);
        if (state.getValue(EAST))
            shape = Shapes.or(shape, SHAPE_EAST);
        if (state.getValue(SOUTH))
            shape = Shapes.or(shape, SHAPE_SOUTH);
        if (state.getValue(WEST))
            shape = Shapes.or(shape, SHAPE_WEST);
        if (state.getValue(GROWTH1))
            shape = Shapes.or(shape, SHAPE_GROWTH1);
        if (state.getValue(GROWTH2))
            shape = Shapes.or(shape, SHAPE_GROWTH2);
        if (state.getValue(GROWTH3))
            shape = Shapes.or(shape, SHAPE_GROWTH3);
        if (state.getValue(GROWTH4))
            shape = Shapes.or(shape, SHAPE_GROWTH4);
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return computeState(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess scheduledAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return computeState(state, level, pos);
    }

    private static BlockState computeState(BlockState state, LevelReader level, BlockPos pos) {
        boolean north = canAttachTo(level, pos.north(), Direction.SOUTH);
        boolean east = canAttachTo(level, pos.east(), Direction.WEST);
        boolean south = canAttachTo(level, pos.south(), Direction.NORTH);
        boolean west = canAttachTo(level, pos.west(), Direction.EAST);
        boolean up = canAttachTo(level, pos.above(), Direction.DOWN);
        boolean down = canAttachTo(level, pos.below(), Direction.UP);
        int q = RandomSource.create(pos.asLong()).nextInt(GROWTH_RNG_RANGE);
        boolean growth1 = down && q < GROWTH1_THRESHOLD;
        boolean growth2 = down && (q == GROWTH2_THRESHOLD_LO || q == GROWTH2_THRESHOLD_HI);
        boolean growth3 = down && q == GROWTH3_VALUE;
        boolean growth4 = up && q > GROWTH4_THRESHOLD;
        return state.setValue(NORTH, north).setValue(EAST, east).setValue(SOUTH, south).setValue(WEST, west).setValue(UP, up).setValue(DOWN, down).setValue(GROWTH1, growth1).setValue(GROWTH2, growth2)
                .setValue(GROWTH3, growth3).setValue(GROWTH4, growth4);
    }

    private static boolean canAttachTo(LevelReader level, BlockPos pos, Direction facing) {
        BlockState neighborState = level.getBlockState(pos);
        return neighborState.isFaceSturdy(level, pos, facing);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (canAttachTo(level, pos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean hasGrowth = state.getValue(GROWTH1) || state.getValue(GROWTH2) || state.getValue(GROWTH3) || state.getValue(GROWTH4);
        if (!hasGrowth && isOnlyAdjacentToTaint(level, pos)) {
            die(level, pos, state);
            return;
        }
        if (!TaintHelper.isNearTaintSeed(level, pos)) {
            die(level, pos, state);
            return;
        }
        TaintHelper.spreadFibres(level, pos, false);
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (living instanceof ITaintedMob) {
            return;
        }
        if (living.is(EntityTypeTags.UNDEAD)) {
            return;
        }
        if (serverLevel.getRandom().nextInt(WALK_EFFECT_CHANCE) == 0) {
            living.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, WALK_EFFECT_DURATION, 0, true, false, false));
        }
    }

    public static boolean isOnlyAdjacentToTaint(LevelAccessor level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.isAir())
                continue;
            if (neighbor.getBlock() instanceof ITaintBlock)
                continue;
            if (!neighbor.isFaceSturdy(level, pos.relative(direction), direction.getOpposite()))
                continue;
            return false;
        }
        return true;
    }

    public static boolean isHemmedByTaint(LevelAccessor level, BlockPos pos) {
        int c = 0;
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.getBlock() instanceof ITaintBlock) {
                c++;
            } else if (neighbor.isAir()) {
                c--;
            } else if (!neighbor.liquid() && !neighbor.isFaceSturdy(level, pos.relative(direction), direction.getOpposite())) {
                c--;
            }
        }
        return c > 0;
    }
}
