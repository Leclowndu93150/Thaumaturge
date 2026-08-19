package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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

    private static final VoxelShape SHAPE_TOP = box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0);
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    public BlockResearchTable(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, ResearchTablePart.MAIN));
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            Direction legSide = facing.getOpposite();
            VoxelShape legs = switch (legSide) {
                case WEST -> Shapes.or(box(2.0, 0.0, 2.0, 6.0, 12.0, 6.0), box(2.0, 0.0, 10.0, 6.0, 12.0, 14.0));
                case EAST -> Shapes.or(box(10.0, 0.0, 2.0, 14.0, 12.0, 6.0), box(10.0, 0.0, 10.0, 14.0, 12.0, 14.0));
                case NORTH -> Shapes.or(box(2.0, 0.0, 2.0, 6.0, 12.0, 6.0), box(10.0, 0.0, 2.0, 14.0, 12.0, 6.0));
                default -> Shapes.or(box(2.0, 0.0, 10.0, 6.0, 12.0, 14.0), box(10.0, 0.0, 10.0, 14.0, 12.0, 14.0));
            };
            shapes.put(facing, Shapes.or(SHAPE_TOP, legs));
        }
        return shapes;
    }

    @Override
    protected MapCodec<BlockResearchTable> codec() {
        return CODEC;
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
        if (!level.getBlockState(partnerPos).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(partnerPos)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, facing).setValue(PART, ResearchTablePart.MAIN);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && state.getValue(PART) == ResearchTablePart.MAIN) {
            Direction facing = state.getValue(FACING);
            level.setBlock(pos.relative(facing), defaultBlockState().setValue(FACING, facing.getOpposite()).setValue(PART, ResearchTablePart.EXT), 3);
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
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
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
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TCBlockEntities.RESEARCH_TABLE.get(), BlockEntityResearchTable::serverTick);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof BlockEntityResearchTable be) {
            be.dropContents(level, pos);
        }
        Direction facing = state.getValue(FACING);
        BlockPos partnerPos = pos.relative(facing);
        BlockState partner = level.getBlockState(partnerPos);
        if (partner.is(this) && partner.getValue(FACING) == facing.getOpposite() && partner.getValue(PART) != state.getValue(PART)) {
            if (level.getBlockEntity(partnerPos) instanceof BlockEntityResearchTable partnerBe) {
                partnerBe.dropContents(level, partnerPos);
                level.removeBlockEntity(partnerPos);
            }
            level.setBlock(partnerPos, TCBlocks.TABLE_WOOD.get().defaultBlockState(), 3);
        }
    }
}
