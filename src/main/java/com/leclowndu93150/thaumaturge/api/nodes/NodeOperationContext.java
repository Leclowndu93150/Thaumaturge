package com.leclowndu93150.thaumaturge.api.nodes;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record NodeOperationContext(ServerLevel level, BlockPos pos, Optional<ServerPlayer> player, Identifier reason) {
    public NodeOperationContext {
        Objects.requireNonNull(level);
        pos = pos.immutable();
        Objects.requireNonNull(player);
        Objects.requireNonNull(reason);
    }

    public static NodeOperationContext player(ServerPlayer player, BlockPos pos, Identifier reason) {
        return new NodeOperationContext(player.level(), pos, Optional.of(player), reason);
    }

    /** Trusted server automation only; never use this factory to service an unvalidated client request. */
    public static NodeOperationContext server(ServerLevel level, BlockPos pos, Identifier reason) {
        return new NodeOperationContext(level, pos, Optional.empty(), reason);
    }
}
