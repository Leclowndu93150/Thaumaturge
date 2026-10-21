package com.leclowndu93150.thaumaturge.api.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jspecify.annotations.Nullable;

/**
 * Permission check before an addon taints a biome column. Fired on the NeoForge event bus,
 * on the server thread, before any mutation. Protection integrations may cancel the change.
 * The affected area is the four-by-four block column containing {@link #position()}, over
 * the level's entire build height. Listeners must not mutate the world.
 *
 * @since 1.0.4
 */
public final class TaintBiomeEvent extends Event implements ICancellableEvent {
    private final ServerLevel level;
    private final BlockPos position;
    private final @Nullable ServerPlayer player;

    public TaintBiomeEvent(ServerLevel level, BlockPos position, @Nullable ServerPlayer player) {
        this.level = level;
        this.position = position.immutable();
        this.player = player;
    }

    /** @return the level being changed */
    public ServerLevel level() {
        return level;
    }

    /** @return the requested position inside the affected column */
    public BlockPos position() {
        return position;
    }

    /** @return the initiating player, or null for a trusted server mechanic */
    public @Nullable ServerPlayer player() {
        return player;
    }
}
