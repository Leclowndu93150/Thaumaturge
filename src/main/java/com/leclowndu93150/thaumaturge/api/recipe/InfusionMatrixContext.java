package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Identity of the real infusion Matrix used to evaluate or start a craft. */
public record InfusionMatrixContext(ServerLevel level, BlockPos position, UUID actingPlayer) {
    public InfusionMatrixContext {
        Objects.requireNonNull(level, "level");
        position = Objects.requireNonNull(position, "position").immutable();
        Objects.requireNonNull(actingPlayer, "actingPlayer");
    }
}
