package com.leclowndu93150.thaumaturge.content.essentia.bellows;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockBellows extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final MapCodec<BlockBellows> CODEC = simpleCodec(BlockBellows::new);
    private static final Map<Direction, VoxelShape> SHAPES = DeviceShapes.facingShapesFromNorth(Shapes.or(
            Block.box(2.0, 2.0, 2.0, 14.0, 4.0, 14.0),
            Block.box(2.0, 7.0, 2.0, 14.0, 9.0, 14.0),
            Block.box(2.0, 12.0, 2.0, 14.0, 14.0, 14.0),
            Block.box(3.0, 5.0, 3.0, 13.0, 7.0, 13.0),
            Block.box(3.0, 9.0, 3.0, 13.0, 11.0, 13.0),
            Block.box(5.0, 4.0, 5.0, 11.0, 5.0, 11.0),
            Block.box(5.0, 11.0, 5.0, 11.0, 12.0, 11.0),
            Block.box(6.0, 6.0, 1.0, 10.0, 10.0, 2.0),
            Block.box(7.0, 7.0, 0.0, 9.0, 9.0, 1.0)));

    public BlockBellows(Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ENABLED, true));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityBellows(blockPos, blockState);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getClickedFace().getOpposite())
                .setValue(ENABLED, true);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState blockState, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.BELLOWS.get(), BlockEntityBellows::serverTick);
    }
}
