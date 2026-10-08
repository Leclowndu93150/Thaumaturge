package com.leclowndu93150.thaumaturge.content.taint.flux;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface PhysicalFluxBlock {
    int fluxAmount(BlockState state);

    BlockState withFluxAmount(int amount);

    void scheduleFluxTick(ServerLevel level, BlockPos pos);

    float auraFloorPerQuantum();

    float taintWeightPerQuantum();

    int outbreakCost();
}
