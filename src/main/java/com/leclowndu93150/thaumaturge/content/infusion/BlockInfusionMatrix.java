package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockInfusionMatrix extends BaseEntityBlock {
    public static final MapCodec<BlockInfusionMatrix> CODEC = simpleCodec(BlockInfusionMatrix::new);

    public BlockInfusionMatrix(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockInfusionMatrix> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityInfusionMatrix(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.INFUSION_MATRIX.get(), level.isClientSide() ? BlockEntityInfusionMatrix::clientTick : BlockEntityInfusionMatrix::serverTick);
    }
}
