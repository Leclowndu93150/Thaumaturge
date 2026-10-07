package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchCrabSpawner extends BaseEntityBlock {
    public static final MapCodec<BlockEldritchCrabSpawner> CODEC = simpleCodec(BlockEldritchCrabSpawner::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final Map<Direction, VoxelShape> SHAPES = DeviceShapes.facingShapesFromUp(Shapes.or(box(0.0, 0.0, 0.0, 7.0, 1.0, 16.0), box(1.0, 1.0, 5.0, 5.0, 2.0, 11.0),
            box(2.0, 1.0, 3.0, 14.0, 2.0, 5.0), box(2.0, 1.0, 11.0, 14.0, 2.0, 14.0), box(2.0, 2.0, 5.0, 4.0, 3.0, 11.0), box(3.0, 1.0, 2.0, 14.0, 2.0, 3.0), box(3.0, 2.0, 3.0, 7.0, 3.0, 5.0),
            box(3.0, 2.0, 11.0, 7.0, 3.0, 13.0), box(4.0, 1.0, 14.0, 11.0, 2.0, 15.0), box(4.0, 2.0, 5.0, 5.0, 3.0, 6.0), box(4.0, 2.0, 10.0, 5.0, 3.0, 11.0), box(5.0, 1.0, 1.0, 11.0, 2.0, 2.0),
            box(5.0, 1.0, 5.0, 6.0, 2.0, 6.0), box(5.0, 1.0, 10.0, 6.0, 2.0, 11.0), box(5.0, 2.0, 2.0, 10.0, 3.0, 3.0), box(5.0, 2.0, 13.0, 10.0, 3.0, 14.0), box(7.0, 0.0, 0.0, 16.0, 1.0, 7.0),
            box(7.0, 0.0, 9.0, 16.0, 1.0, 16.0), box(7.0, 2.0, 3.0, 13.0, 3.0, 4.0), box(7.0, 2.0, 12.0, 13.0, 3.0, 13.0), box(9.0, 0.0, 7.0, 16.0, 1.0, 9.0), box(9.0, 2.0, 11.0, 13.0, 3.0, 12.0),
            box(10.0, 1.0, 5.0, 15.0, 2.0, 6.0), box(10.0, 1.0, 10.0, 15.0, 2.0, 11.0), box(10.0, 2.0, 4.0, 13.0, 3.0, 5.0), box(11.0, 1.0, 6.0, 15.0, 2.0, 10.0), box(11.0, 2.0, 5.0, 14.0, 3.0, 6.0),
            box(11.0, 2.0, 10.0, 13.0, 3.0, 11.0), box(12.0, 2.0, 6.0, 13.0, 3.0, 10.0), box(13.0, 2.0, 6.0, 14.0, 3.0, 9.0)));

    private static final int XP_BASE = 15;
    private static final int XP_ROLL = 15;

    public BlockEldritchCrabSpawner(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<BlockEldritchCrabSpawner> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
        RandomSource random = level.getRandom();
        return XP_BASE + random.nextInt(XP_ROLL) + random.nextInt(XP_ROLL);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEldritchCrabSpawner(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TTBlockEntities.ELDRITCH_CRAB_SPAWNER.get(), (tickLevel, pos, tickState, spawner) -> spawner.tick(tickLevel, pos, tickState));
    }
}
