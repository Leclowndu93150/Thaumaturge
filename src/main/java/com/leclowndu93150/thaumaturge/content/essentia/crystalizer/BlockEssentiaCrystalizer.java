package com.leclowndu93150.thaumaturge.content.essentia.crystalizer;

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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockEssentiaCrystalizer extends BaseEntityBlock {
    public static final MapCodec<BlockEssentiaCrystalizer> CODEC = simpleCodec(BlockEssentiaCrystalizer::new);
    private static final VoxelShape BODY = Shapes.or(Block.box(5.0, 0.0, 5.0, 11.0, 1.0, 11.0), Block.box(2.0, 1.0, 2.0, 14.0, 3.0, 14.0), Block.box(5.0, 3.0, 5.0, 11.0, 10.0, 11.0),
            Block.box(4.0, 4.0, 4.0, 12.0, 5.0, 12.0), Block.box(4.0, 8.0, 4.0, 12.0, 9.0, 12.0), Block.box(3.0, 10.0, 3.0, 13.0, 11.0, 13.0), Block.box(3.0, 11.0, 3.0, 4.0, 14.0, 4.0),
            Block.box(12.0, 11.0, 3.0, 13.0, 14.0, 4.0), Block.box(3.0, 11.0, 12.0, 4.0, 14.0, 13.0), Block.box(12.0, 11.0, 12.0, 13.0, 14.0, 13.0));
    private static final VoxelShape CRYSTALS = Shapes.or(Block.box(6.5, 11.0, 6.5, 9.5, 17.0, 9.5), Block.box(6.0, 11.0, 6.0, 10.0, 14.0, 10.0), Block.box(4.0, 11.0, 5.0, 6.0, 15.2, 7.0),
            Block.box(3.5, 11.0, 4.5, 6.5, 13.1, 7.5), Block.box(9.0, 11.0, 10.0, 11.0, 15.2, 12.0), Block.box(8.5, 11.0, 9.5, 11.5, 13.1, 12.5));
    private static final Map<Direction, VoxelShape> SHAPES = DeviceShapes.facingShapesFromDown(BODY);
    private static final Map<Direction, VoxelShape> GROWING_SHAPES = DeviceShapes.facingShapesFromDown(Shapes.or(BODY, CRYSTALS));

    public BlockEssentiaCrystalizer(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.DOWN));
    }

    @Override
    protected MapCodec<BlockEssentiaCrystalizer> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.FACING, rotation.rotate(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        return level.getBlockEntity(pos) instanceof BlockEntityEssentiaCrystalizer crystalizer && crystalizer.aspectKey() != null ? GROWING_SHAPES.get(facing) : SHAPES.get(facing);
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEssentiaCrystalizer(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TTBlockEntities.ESSENTIA_CRYSTALIZER.get(), level.isClientSide() ? BlockEntityEssentiaCrystalizer::clientTick : BlockEntityEssentiaCrystalizer::serverTick);
    }
}
