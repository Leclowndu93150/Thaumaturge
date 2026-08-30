package com.leclowndu93150.thaumaturge.api.aura;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Stable server-level identity and position of an addon vis-relay source endpoint. */
public record VisRelaySourceContext(ServerLevel level, UUID hostIdentity, BlockPos position) {
    public VisRelaySourceContext {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(hostIdentity, "hostIdentity");
        position = Objects.requireNonNull(position, "position").immutable();
    }
}
