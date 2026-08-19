package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStreamPort;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockMirror extends BaseEntityBlock implements IEssentiaStreamPort {
    public static final MapCodec<BlockMirror> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(propertiesCodec(), Codec.BOOL.fieldOf("essentia").forGetter(BlockMirror::isEssentia)).apply(inst, BlockMirror::new));
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final double PANE_SURFACE = -0.34;
    private static final double PANE_CLEARANCE = 0.3;

    private static final VoxelShape SHAPE_UP = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    private static final VoxelShape SHAPE_DOWN = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_WEST = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    private final boolean essentia;

    public BlockMirror(BlockBehaviour.Properties properties, boolean essentia) {
        super(properties);
        this.essentia = essentia;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public boolean isEssentia() {
        return essentia;
    }

    @Override
    protected MapCodec<BlockMirror> codec() {
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
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_UP;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public StreamPort essentiaStreamPort(BlockGetter level, BlockPos pos, BlockState state, Vec3 farEnd, boolean outgoing) {
        Direction facing = state.getValue(FACING);
        Vec3 normal = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vec3 center = Vec3.atCenterOf(pos);
        return new StreamPort(center.add(normal.scale(PANE_SURFACE)), center.add(normal.scale(PANE_CLEARANCE)));
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
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!level.isClientSide() && !essentia && entity instanceof ItemEntity itemEntity && entity.isAlive() && !entity.isOnPortalCooldown()
                && level.getBlockEntity(pos) instanceof BlockEntityMirror mirror) {
            mirror.transport(itemEntity);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return essentia ? new BlockEntityMirrorEssentia(pos, state) : new BlockEntityMirror(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        if (essentia) {
            return createTickerHelper(type, TCBlockEntities.MIRROR_ESSENTIA.get(), (tickLevel, pos, tickState, mirror) -> mirror.serverTick(tickLevel, pos));
        }
        return createTickerHelper(type, TCBlockEntities.MIRROR.get(), (tickLevel, pos, tickState, mirror) -> mirror.serverTick(tickLevel, pos));
    }
}
