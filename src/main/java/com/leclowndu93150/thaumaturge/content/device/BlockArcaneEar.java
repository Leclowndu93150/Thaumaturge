package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockArcaneEar extends BaseEntityBlock {
    public static final MapCodec<BlockArcaneEar> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codec.BOOL.fieldOf("toggle").forGetter(block -> block.toggle), propertiesCodec())
                    .apply(instance, BlockArcaneEar::new));

    private static final VoxelShape EAR = Shapes.or(
            box(3.0, 0.0, 3.0, 13.0, 2.0, 13.0),
            box(5.0, 2.0, 5.0, 11.0, 3.0, 11.0),
            box(7.0, 3.0, 7.0, 9.0, 6.0, 9.0),
            box(6.0, 6.0, 6.0, 10.0, 7.0, 10.0),
            box(5.0, 7.0, 5.0, 11.0, 8.0, 11.0),
            box(4.0, 8.0, 4.0, 5.0, 10.0, 12.0),
            box(11.0, 8.0, 5.0, 12.0, 10.0, 11.0),
            box(5.0, 8.0, 4.0, 12.0, 10.0, 5.0),
            box(5.0, 8.0, 11.0, 12.0, 10.0, 12.0));

    private static final VoxelShape SWITCH_OFF =
            Shapes.or(box(5.0, 2.0, 12.0, 6.0, 5.0, 13.0), box(6.0, 2.0, 12.0, 9.0, 3.0, 13.0));

    private static final VoxelShape SWITCH_ON =
            Shapes.or(box(5.0, 2.0, 12.0, 9.0, 3.0, 13.0), box(8.0, 3.0, 12.0, 9.0, 5.0, 13.0));

    private static final Map<Direction, VoxelShape> EAR_SHAPES = DeviceShapes.facingShapesFromUp(EAR);

    private static final Map<Direction, VoxelShape> TOGGLE_OFF_SHAPES =
            DeviceShapes.facingShapesFromUp(Shapes.or(EAR, SWITCH_OFF));

    private static final Map<Direction, VoxelShape> TOGGLE_ON_SHAPES =
            DeviceShapes.facingShapesFromUp(Shapes.or(EAR, SWITCH_ON));

    private final boolean toggle;

    public BlockArcaneEar(boolean toggle, BlockBehaviour.Properties properties) {
        super(properties);
        this.toggle = toggle;
        registerDefaultState(getStateDefinition()
                .any()
                .setValue(BlockStateProperties.FACING, Direction.UP)
                .setValue(BlockStateProperties.ENABLED, false));
    }

    public boolean isToggle() {
        return toggle;
    }

    @Override
    protected MapCodec<BlockArcaneEar> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.ENABLED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Map<Direction, VoxelShape> shapes = !toggle
                ? EAR_SHAPES
                : state.getValue(BlockStateProperties.ENABLED) ? TOGGLE_ON_SHAPES : TOGGLE_OFF_SHAPES;
        return shapes.get(state.getValue(BlockStateProperties.FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(BlockStateProperties.FACING, context.getClickedFace())
                .setValue(BlockStateProperties.ENABLED, false);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction directionToNeighbour,
            BlockState neighbourState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighbourPos) {
        if (!canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, directionToNeighbour, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityArcaneEar(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.getBlockEntity(pos) instanceof BlockEntityArcaneEar ear) {
            ear.updateTone();
        }
    }

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        if (level.getBlockEntity(pos) instanceof BlockEntityArcaneEar ear) {
            ear.updateTone();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityArcaneEar ear) {
            ear.changePitch();
            ear.playNote();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(BlockStateProperties.ENABLED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(BlockStateProperties.ENABLED) ? 15 : 0;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.ARCANE_EAR.get(), BlockEntityArcaneEar::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
