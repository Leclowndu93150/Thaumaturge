package com.leclowndu93150.thaumaturge.content.essentia.crystalizer;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Automated TC4 essentia-to-crystal endpoint. */
public final class BlockEssentiaCrystalizer extends BaseEntityBlock {
    public static final MapCodec<BlockEssentiaCrystalizer> CODEC = simpleCodec(BlockEssentiaCrystalizer::new);
    private static final Map<Direction, VoxelShape> SHAPES =
            DeviceShapes.facingShapesFromDown(Block.box(3.2, 0.0, 3.2, 12.8, 16.0, 12.8));

    public BlockEssentiaCrystalizer(BlockBehaviour.Properties properties) {
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
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(
                BlockStateProperties.FACING, rotation.rotate(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.FACING)));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(BlockStateProperties.FACING));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEssentiaCrystalizer(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(
                type,
                TCBlockEntities.ESSENTIA_CRYSTALIZER.get(),
                level.isClientSide()
                        ? BlockEntityEssentiaCrystalizer::clientTick
                        : BlockEntityEssentiaCrystalizer::serverTick);
    }
}
