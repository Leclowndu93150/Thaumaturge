package com.leclowndu93150.thaumaturge.api.warp;

import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Reduces a server warp roll after the native fortress mask penalty and before outcome selection.
 * Register listeners once on the NeoForge event bus, not during resource reload. Listeners should
 * inspect the player's current equipment and only contribute a reduction; stored warp, event
 * probability, and the normal counter update are independent of this event.
 *
 * <p>Contributions are additive and cannot undo another listener's reduction. NeoForge listener
 * priority determines callback order. Zero strength suppresses the outcome, not the warp check.
 *
 * @since 1.0.4
 */
public final class WarpStrengthEvent extends Event {
    private final ServerPlayer player;
    private final int originalStrength;
    private int strength;

    public WarpStrengthEvent(ServerPlayer player, int originalStrength) {
        this.player = Objects.requireNonNull(player);
        requireServerThread();
        this.originalStrength = originalStrength;
        strength = Math.max(0, originalStrength);
    }

    public ServerPlayer player() {
        return player;
    }

    /** The unmodified roll after the native mask penalty, which can be negative. */
    public int originalStrength() {
        return originalStrength;
    }

    /** The current strength, bounded between zero and the nonnegative original strength. */
    public int strength() {
        return strength;
    }

    /** Adds a nonnegative reduction, saturating at zero without integer overflow. */
    public void reduce(int amount) {
        requireServerThread();
        if (amount < 0) {
            throw new IllegalArgumentException("Warp strength reduction must not be negative");
        }
        strength -= Math.min(strength, amount);
    }

    private void requireServerThread() {
        if (!player.level().getServer().isSameThread()) {
            throw new IllegalStateException("Warp strength modifiers require the server thread");
        }
    }
}
