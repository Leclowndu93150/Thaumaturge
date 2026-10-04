package com.leclowndu93150.thaumaturge.content.essentia.flow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface EssentiaIntakeHost {
    @Nullable
    Level getLevel();

    BlockPos getBlockPos();

    Direction intakeFace();

    boolean wantsEssentia();
}
