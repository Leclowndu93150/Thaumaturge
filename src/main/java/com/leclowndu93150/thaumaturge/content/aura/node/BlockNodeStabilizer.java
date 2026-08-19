package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockNodeStabilizer extends Block implements EntityBlock {
    private static final MapCodec<BlockNodeStabilizer> CODEC = simpleCodec(props -> new BlockNodeStabilizer(props, false));

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
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityNodeStabilizer(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide() || type != TCBlockEntities.NODE_STABILIZER.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, stabilizer) -> ((BlockEntityNodeStabilizer) stabilizer).clientTick(tickLevel, pos);
    }
}
