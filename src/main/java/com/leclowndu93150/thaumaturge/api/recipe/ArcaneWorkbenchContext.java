package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Stable identity and location information for one arcane-crafting transaction.
 *
 * <p>The same instance is used while a transaction is previewed and committed. A placed context
 * has a position; a virtual context deliberately does not. Host identities are supplied by the
 * host and must remain stable across menu openings and server restarts.
 *
 * @since 0.3.2
 */
public record ArcaneWorkbenchContext(
        ServerLevel level,
        UUID hostIdentity,
        @Nullable BlockPos position,
        @Nullable UUID owner,
        UUID actingPlayer,
        Kind kind) {
    public ArcaneWorkbenchContext {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(hostIdentity, "hostIdentity");
        Objects.requireNonNull(actingPlayer, "actingPlayer");
        Objects.requireNonNull(kind, "kind");
        position = position == null ? null : position.immutable();
        if (kind == Kind.PLACED && position == null) {
            throw new IllegalArgumentException("A placed workbench requires a position");
        }
        if (kind == Kind.VIRTUAL && position != null) {
            throw new IllegalArgumentException("A virtual workbench cannot have a block position");
        }
    }

    public static ArcaneWorkbenchContext placed(
            ServerPlayer player, BlockPos position, UUID hostIdentity, @Nullable UUID owner) {
        return new ArcaneWorkbenchContext(
                player.serverLevel(), hostIdentity, position, owner, player.getUUID(), Kind.PLACED);
    }

    public static ArcaneWorkbenchContext virtual(ServerPlayer player, UUID hostIdentity, @Nullable UUID owner) {
        return new ArcaneWorkbenchContext(
                player.serverLevel(), hostIdentity, null, owner, player.getUUID(), Kind.VIRTUAL);
    }

    public Optional<BlockPos> blockPosition() {
        return Optional.ofNullable(position);
    }

    public Optional<UUID> ownerIdentity() {
        return Optional.ofNullable(owner);
    }

    public enum Kind {
        PLACED,
        VIRTUAL
    }
}
