package com.leclowndu93150.thaumaturge.content.eldritch;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class EldritchPlacement {
    private EldritchPlacement() {}

    public static <T extends BlockEntity> Optional<T> place(
            ServerLevel level, BlockPos pos, BlockState state, BlockEntityType<T> type) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
        return level.getBlockEntity(pos, type);
    }

    public static <T extends BlockEntity> Optional<T> restore(
            ServerLevel level, BlockPos pos, Block block, Supplier<BlockState> state, BlockEntityType<T> type) {
        if (!level.isLoaded(pos) || level.getBlockState(pos).is(block)) {
            return Optional.empty();
        }
        return place(level, pos, state.get(), type);
    }
}
