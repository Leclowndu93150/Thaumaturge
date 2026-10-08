package com.leclowndu93150.thaumaturge.content.aura.pressure;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface FluxPressureEvent {
    String name();

    int weight();

    float cost();

    boolean allowedNearTaint();

    boolean fire(ServerLevel level, BlockPos origin, FluxPressureState state);
}
