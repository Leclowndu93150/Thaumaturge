package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStreamPort;
import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockEssentiaPort extends BaseEntityBlock implements IEssentiaStreamPort {
    public static final MapCodec<BlockEssentiaPort> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Codec.BOOL.fieldOf("input").forGetter(block -> block.input), propertiesCodec()).apply(instance, BlockEssentiaPort::new));

    private static final Map<Direction, VoxelShape> SHAPES = DeviceShapes.facingShapesFromUp(Shapes.or(box(4.0, 0.0, 4.0, 12.0, 6.0, 12.0), box(6.0, 6.0, 6.0, 10.0, 8.0, 10.0)));

    private static final double NOZZLE_TIP = 0.05;
    private static final double NOZZLE_CLEARANCE = 0.65;

    private final boolean input;

    public BlockEssentiaPort(boolean input, BlockBehaviour.Properties properties) {
        super(properties);
        this.input = input;
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.FACING, Direction.UP));
    }

    public boolean isInput() {
        return input;
    }

    @Override
    protected MapCodec<BlockEssentiaPort> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(BlockStateProperties.FACING));
    }

    @Override
    public StreamPort essentiaStreamPort(BlockGetter level, BlockPos pos, BlockState state, Vec3 farEnd, boolean outgoing) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        Vec3 normal = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vec3 center = Vec3.atCenterOf(pos);
        return new StreamPort(center.add(normal.scale(NOZZLE_TIP)), center.add(normal.scale(NOZZLE_CLEARANCE)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEssentiaPort(pos, state, input);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TCBlockEntities.ESSENTIA_PORT.get(), BlockEntityEssentiaPort::serverTick);
    }
}
