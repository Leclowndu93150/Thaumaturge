package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Identifies the infusion matrix an inspection or craft start runs against, and the player acting
 * on it.
 *
 * @param level        the server level holding the matrix
 * @param position     the matrix block position
 * @param actingPlayer the UUID of the player inspecting or starting the craft
 * @since 1.0.0
 */
public record InfusionMatrixContext(ServerLevel level, BlockPos position, UUID actingPlayer) {
    /**
     * Validates the components and stores an immutable copy of the position.
     *
     * @throws NullPointerException when a component is null
     */
    public InfusionMatrixContext {
        Objects.requireNonNull(level, "level");
        position = Objects.requireNonNull(position, "position").immutable();
        Objects.requireNonNull(actingPlayer, "actingPlayer");
    }

    /**
     * Creates a context for a matrix in the player's level.
     *
     * @param player   the acting player
     * @param position the matrix block position
     * @return the context
     */
    public static InfusionMatrixContext of(ServerPlayer player, BlockPos position) {
        return new InfusionMatrixContext(player.serverLevel(), position, player.getUUID());
    }
}
