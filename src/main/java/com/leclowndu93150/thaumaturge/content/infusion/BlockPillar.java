package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockPillar extends HorizontalDirectionalBlock {
    public static final MapCodec<BlockPillar> CODEC = simpleCodec(BlockPillar::new);

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            box(3.0, 8.0, 3.0, 13.0, 19.0, 13.0),
            box(5.0, 19.0, 5.0, 12.0, 20.0, 12.0),
            box(4.0, 20.0, 4.0, 12.0, 23.0, 12.0),
            box(4.0, 21.0, 12.0, 13.0, 23.0, 13.0),
            box(12.0, 22.0, 5.0, 13.0, 25.0, 13.0),
            box(5.0, 23.0, 5.0, 7.0, 27.0, 13.0),
            box(7.0, 23.0, 5.0, 12.0, 25.0, 13.0),
            box(7.0, 25.0, 5.0, 13.0, 26.0, 13.0),
            box(12.0, 25.0, 13.0, 14.0, 26.0, 14.0),
            box(7.0, 26.0, 5.0, 10.0, 27.0, 8.0),
            box(7.0, 26.0, 7.0, 15.0, 28.0, 15.0),
            box(10.0, 27.0, 15.0, 15.0, 31.0, 16.0),
            box(7.0, 28.0, 10.0, 15.0, 29.0, 15.0),
            box(8.0, 28.0, 8.0, 15.0, 30.0, 10.0),
            box(15.0, 28.0, 12.0, 16.0, 30.0, 14.0),
            box(8.0, 29.0, 9.0, 16.0, 31.0, 15.0),
            box(14.0, 30.0, 11.0, 15.0, 32.0, 13.0),
            box(10.0, 31.0, 10.0, 14.0, 32.0, 15.0),
            box(11.0, 32.0, 11.0, 13.0, 33.0, 14.0));

    private static final Map<Direction, VoxelShape> SHAPES = shapes();

    public BlockPillar(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<BlockPillar> codec() {
        return CODEC;
    }

    private static Map<Direction, VoxelShape> shapes() {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, NORTH_SHAPE);
        shapes.put(Direction.EAST, DeviceShapes.rotate(NORTH_SHAPE, 0, 1));
        shapes.put(Direction.SOUTH, DeviceShapes.rotate(NORTH_SHAPE, 0, 2));
        shapes.put(Direction.WEST, DeviceShapes.rotate(NORTH_SHAPE, 0, 3));
        return shapes;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
