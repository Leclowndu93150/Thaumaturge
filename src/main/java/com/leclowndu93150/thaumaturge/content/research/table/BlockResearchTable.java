package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockResearchTable extends BaseEntityBlock {
    public static final MapCodec<BlockResearchTable> CODEC = simpleCodec(BlockResearchTable::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ResearchTablePart> PART = EnumProperty.create("part", ResearchTablePart.class);

    private static final VoxelShape TRESTLE = Shapes.or(
            box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0),
            box(2.0, 11.0, 0.0, 3.0, 14.0, 12.0),
            box(4.0, 11.0, 12.0, 12.0, 14.0, 13.0),
            box(2.0, 1.0, 12.0, 4.0, 14.0, 14.0),
            box(12.0, 1.0, 12.0, 14.0, 14.0, 14.0),
            box(4.0, 7.0, 12.0, 12.0, 9.0, 14.0),
            box(4.0, 2.0, 12.0, 12.0, 3.0, 14.0),
            box(1.0, 0.0, 11.0, 15.0, 1.0, 15.0),
            box(7.0, 3.0, 0.0, 9.0, 5.0, 14.0),
            box(7.0, 2.0, 14.0, 9.0, 6.0, 15.0));

    private static final Map<Direction, VoxelShape> MAIN_SHAPES = DeviceShapes.facingShapesFromNorth(Shapes.or(
            TRESTLE,
            box(13.0, 11.0, 0.0, 14.0, 14.0, 3.0),
            box(9.0, 11.0, 3.0, 13.0, 14.0, 12.0),
            box(13.0, 10.0, 3.0, 14.0, 14.0, 12.0),
            box(14.0, 11.0, 4.0, 15.0, 13.0, 11.0),
            box(15.0, 11.0, 7.0, 16.0, 13.0, 8.0)));

    private static final Map<Direction, VoxelShape> EXT_SHAPES =
            DeviceShapes.facingShapesFromNorth(Shapes.or(TRESTLE, box(13.0, 11.0, 0.0, 14.0, 14.0, 12.0)));

    public BlockResearchTable(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, ResearchTablePart.MAIN));
    }

    @Override
    protected MapCodec<BlockResearchTable> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getClockWise();
        BlockPos partnerPos = context.getClickedPos().relative(facing);
        Level level = context.getLevel();
        if (!level.getBlockState(partnerPos).canBeReplaced(context)
                || !level.getWorldBorder().isWithinBounds(partnerPos)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, facing).setValue(PART, ResearchTablePart.MAIN);
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && state.getValue(PART) == ResearchTablePart.MAIN) {
            Direction facing = state.getValue(FACING);
            level.setBlock(
                    pos.relative(facing),
                    defaultBlockState().setValue(FACING, facing.getOpposite()).setValue(PART, ResearchTablePart.EXT),
                    3);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Map<Direction, VoxelShape> shapes = state.getValue(PART) == ResearchTablePart.MAIN ? MAIN_SHAPES : EXT_SHAPES;
        return shapes.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == ResearchTablePart.MAIN ? new BlockEntityResearchTable(pos, state) : null;
    }

    public static BlockPos mainPos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == ResearchTablePart.MAIN ? pos : pos.relative(state.getValue(FACING));
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(mainPos(state, pos)) instanceof BlockEntityResearchTable be ? be : null;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockPos main = mainPos(state, pos);
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider != null) {
                player.openMenu(provider, buf -> buf.writeBlockPos(main));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.RESEARCH_TABLE.get(), BlockEntityResearchTable::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BlockEntityResearchTable be) {
                be.dropContents(level, pos);
            }
            Direction facing = state.getValue(FACING);
            BlockPos partnerPos = pos.relative(facing);
            BlockState partner = level.getBlockState(partnerPos);
            if (partner.is(this)
                    && partner.getValue(FACING) == facing.getOpposite()
                    && partner.getValue(PART) != state.getValue(PART)) {
                if (level.getBlockEntity(partnerPos) instanceof BlockEntityResearchTable partnerBe) {
                    partnerBe.dropContents(level, partnerPos);
                    level.removeBlockEntity(partnerPos);
                }
                level.setBlock(partnerPos, TTBlocks.TABLE_WOOD.get().defaultBlockState(), 3);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
