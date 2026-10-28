package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityJar extends AbstractBlockEntityJar {
    public BlockEntityJar(BlockPos pos, BlockState state) {
        super(TTBlockEntities.JAR.get(), pos, state);
    }
}
