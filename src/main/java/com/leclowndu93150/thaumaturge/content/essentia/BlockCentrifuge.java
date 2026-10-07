package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockCentrifuge extends BaseEntityBlock {
    public static final MapCodec<BlockCentrifuge> CODEC = simpleCodec(BlockCentrifuge::new);

    private static final VoxelShape SHAPE = Shapes.or(box(5.0, 0.0, 5.0, 11.0, 1.0, 11.0), box(3.0, 1.0, 3.0, 13.0, 15.0, 13.0), box(5.0, 15.0, 5.0, 11.0, 16.0, 11.0));

    public BlockCentrifuge(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockCentrifuge> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityCentrifuge(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TTBlockEntities.CENTRIFUGE.get(), level.isClientSide() ? BlockEntityCentrifuge::clientTick : BlockEntityCentrifuge::serverTick);
    }
}
