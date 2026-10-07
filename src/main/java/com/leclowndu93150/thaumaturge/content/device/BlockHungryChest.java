package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockHungryChest extends ChestBlock {
    public static final MapCodec<BlockHungryChest> CODEC = simpleCodec(BlockHungryChest::new);

    public BlockHungryChest(BlockBehaviour.Properties properties) {
        super(properties, () -> TTBlockEntities.HUNGRY_CHEST.get());
    }

    @Override
    public MapCodec<BlockHungryChest> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityHungryChest(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, TTBlockEntities.HUNGRY_CHEST.get(), ChestBlockEntity::lidAnimateTick);
        }
        return createTickerHelper(type, TTBlockEntities.HUNGRY_CHEST.get(), BlockEntityHungryChest::serverTick);
    }
}
