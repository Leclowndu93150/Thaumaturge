package com.leclowndu93150.thaumaturge.api.aura;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Native biome baseline recalculation. Existing vis and flux are always preserved. */
public final class AuraBaseline {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("AuraBaseline");

    private AuraBaseline() {}

    public record Context(ServerLevel level, ChunkPos chunk, Optional<ServerPlayer> player, Optional<UUID> delegation) {
        public Context {
            Objects.requireNonNull(level);
            Objects.requireNonNull(chunk);
            Objects.requireNonNull(player);
            Objects.requireNonNull(delegation);
            if (player.isPresent() == delegation.isPresent()) {
                throw new IllegalArgumentException("Provide exactly one aura operation authority");
            }
        }

        public static Context player(ServerPlayer player, ChunkPos chunk) {
            return new Context(player.level(), chunk, Optional.of(player), Optional.empty());
        }

        public static Context delegated(ServerLevel level, ChunkPos chunk, UUID token) {
            return new Context(level, chunk, Optional.empty(), Optional.of(token));
        }
    }

    /** The biome revision fingerprints the five sampled biome ids and current data-map modifiers. */
    public record Quote(ResourceKey<Level> dimension, ChunkPos chunk, long auraRevision, UUID biomeRevision, short currentBase, short proposedBase) {
    }

    public enum Result {
        APPLIED, ALREADY_APPLIED, STALE, DENIED, UNAVAILABLE, BUSY, LIMIT_REACHED, INVALID
    }

    public static Optional<Quote> quote(Context context) {
        return BINDING.get().quote(context);
    }

    /**
     * Stages the baseline and durable operation receipt in the caller's transaction. Use a strictly
     * increasing positive sequence per authority and chunk, persisted by the caller before retrying.
     * Charge resources only after APPLIED, in this same transaction. Abort on every other outcome;
     * ALREADY_APPLIED acknowledges a committed retry without charging again. Do not yield, mutate
     * biomes or unload chunks before closing the transaction. An old quote must be refreshed after
     * STALE; keep the same sequence for a pending operation. Cross-storage crash recovery of the
     * caller's payment remains the caller's responsibility.
     */
    public static Result recalculate(Context context, Quote expected, long sequence, TransactionContext transaction) {
        return BINDING.get().recalculate(context, expected, sequence, transaction);
    }

    /** Authorizes one loaded, owned machine for one chunk, for at most seven game days. */
    public static Optional<UUID> delegate(ServerPlayer owner, BlockPos machine, ChunkPos target, long durationTicks) {
        return BINDING.get().delegate(owner, machine, target, durationTicks);
    }

    public static boolean revoke(ServerPlayer owner, UUID delegation) {
        return BINDING.get().revoke(owner, delegation);
    }

    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    public interface Bindings {
        Optional<Quote> quote(Context context);
        Result recalculate(Context context, Quote expected, long sequence, TransactionContext transaction);
        Optional<UUID> delegate(ServerPlayer owner, BlockPos machine, ChunkPos target, long durationTicks);
        boolean revoke(ServerPlayer owner, UUID delegation);
    }
}
