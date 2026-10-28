package com.leclowndu93150.thaumaturge.content.legacy;

import com.leclowndu93150.thaumaturge.content.essentia.jar.AbstractBlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.AbstractBlockJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class LegacyJarEntities {
    private LegacyJarEntities() {}

    public static AbstractBlockEntityJar create(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof AbstractBlockJar jar && jar.jarType() != TTBlockEntities.JAR.get()) {
            return jar.jarType().create(pos, state);
        }
        return new BlockEntityJar(pos, state);
    }
}
