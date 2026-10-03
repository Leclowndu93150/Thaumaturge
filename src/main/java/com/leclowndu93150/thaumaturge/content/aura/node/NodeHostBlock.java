package com.leclowndu93150.thaumaturge.content.aura.node;

import net.minecraft.world.level.block.state.BlockState;

public interface NodeHostBlock {
    BlockState depletedState(BlockState state);
}
