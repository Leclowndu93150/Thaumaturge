package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockNodeStabilizer extends Block implements EntityBlock {
    private static final MapCodec<BlockNodeStabilizer> CODEC =
            simpleCodec(props -> new BlockNodeStabilizer(props, false));

    public static final VoxelShape SHAPE = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            box(1.0, 2.0, 5.0, 15.0, 3.0, 11.0),
            box(0.0, 3.0, 5.0, 16.0, 4.0, 11.0),
            box(1.0, 4.0, 5.0, 15.0, 5.0, 11.0),
            box(0.0, 5.0, 6.0, 16.0, 7.0, 10.0),
            box(2.0, 5.0, 5.0, 14.0, 6.0, 6.0),
            box(2.0, 5.0, 10.0, 14.0, 6.0, 11.0),
            box(1.0, 7.0, 6.0, 15.0, 8.0, 10.0),
            box(3.0, 2.0, 3.0, 13.0, 8.0, 5.0),
            box(3.0, 2.0, 11.0, 13.0, 8.0, 13.0),
            box(3.0, 6.0, 5.0, 13.0, 8.0, 6.0),
            box(3.0, 6.0, 10.0, 13.0, 8.0, 11.0),
            box(4.0, 8.0, 4.0, 12.0, 9.0, 12.0),
            box(6.0, 9.0, 6.0, 10.0, 10.0, 10.0),
            box(5.0, 2.0, 1.0, 11.0, 5.0, 3.0),
            box(5.0, 2.0, 13.0, 11.0, 5.0, 15.0),
            box(5.0, 3.0, 0.0, 11.0, 4.0, 1.0),
            box(5.0, 3.0, 15.0, 11.0, 4.0, 16.0),
            box(5.0, 5.0, 2.0, 11.0, 6.0, 3.0),
            box(5.0, 5.0, 13.0, 11.0, 6.0, 14.0),
            box(6.0, 5.0, 0.0, 10.0, 7.0, 2.0),
            box(6.0, 5.0, 14.0, 10.0, 7.0, 16.0),
            box(6.0, 6.0, 2.0, 10.0, 8.0, 3.0),
            box(6.0, 6.0, 13.0, 10.0, 8.0, 14.0),
            box(6.0, 7.0, 1.0, 10.0, 8.0, 2.0),
            box(6.0, 7.0, 14.0, 10.0, 8.0, 15.0));

    private final boolean advanced;

    public BlockNodeStabilizer(BlockBehaviour.Properties properties, boolean advanced) {
        super(properties);
        this.advanced = advanced;
    }

    @Override
    protected MapCodec<BlockNodeStabilizer> codec() {
        return CODEC;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityNodeStabilizer(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide() || type != TTBlockEntities.NODE_STABILIZER.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, stabilizer) ->
                ((BlockEntityNodeStabilizer) stabilizer).clientTick(tickLevel, pos);
    }
}
