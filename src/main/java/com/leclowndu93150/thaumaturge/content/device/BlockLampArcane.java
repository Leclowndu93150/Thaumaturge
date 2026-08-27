package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockLampArcane extends BlockLamp {
    public static final MapCodec<BlockLampArcane> CODEC = simpleCodec(BlockLampArcane::new);

    public BlockLampArcane(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockLampArcane> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityLampArcane(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TCBlockEntities.LAMP_ARCANE.get(), BlockEntityLampArcane::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BlockEntityLampArcane lamp) {
                lamp.removeLights();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
