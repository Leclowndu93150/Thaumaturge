package com.leclowndu93150.thaumaturge.api.rifts;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/** Server operation identity; UUIDs alone never authorize an offline operation. */
public record RiftOperationContext(ServerLevel level, @Nullable ServerPlayer player, @Nullable BlockPos machinePosition, RiftAuthorization.@Nullable Handle authorization, Identifier reason) {
    public RiftOperationContext {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(reason, "reason");
        if ((player == null) == (authorization == null)) {
            throw new IllegalArgumentException("Exactly one player or machine authorization is required");
        }
        if ((machinePosition == null) != (authorization == null)) {
            throw new IllegalArgumentException("Machine position and authorization must be supplied together");
        }
        if (machinePosition != null) {
            machinePosition = machinePosition.immutable();
        }
    }

    public static RiftOperationContext player(ServerPlayer player, Identifier reason) {
        return new RiftOperationContext(player.level(), player, null, null, reason);
    }

    public static RiftOperationContext machine(ServerLevel level, BlockPos position, RiftAuthorization.Handle authorization, Identifier reason) {
        return new RiftOperationContext(level, null, Objects.requireNonNull(position, "position"), Objects.requireNonNull(authorization, "authorization"), reason);
    }
}
