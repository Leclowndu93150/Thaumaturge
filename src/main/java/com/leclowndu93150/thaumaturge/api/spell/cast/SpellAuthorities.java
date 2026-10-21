package com.leclowndu93150.thaumaturge.api.spell.cast;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Server issuance and revocation for automated spells. Client-supplied identities are not authority.
 *
 * <p>Scoped casts reject uncontrolled explosions, wind explosions, bonemeal growth, portable-hole
 * propagation, and Primal chaos. Those operations can alter targets after the checked effect has
 * returned and cannot safely honor a revocable scope. Authorized hellbats retain their grant and
 * use checked melee attacks instead of native explosions. Native casts retain their existing behavior.
 *
 * <p>New native continuations retain their nullable-caster behavior across saves. Older continuations
 * without provenance are rejected when their caster cannot be resolved; they never acquire inferred
 * machine authority. Pending {@code proceedLater} work remains an in-memory queue.
 */
public final class SpellAuthorities {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("SpellAuthorities");

    private SpellAuthorities() {}

    /** Implemented by an automated source using its current server-owned owner identity. */
    public interface Source {
        UUID spellOwner();
    }

    /**
     * Checks current addon protection and target rules. Called at grant, start and execution;
     * implementations must deny unknown permissions, including unavailable offline owner state.
     */
    @FunctionalInterface
    public interface Resolver {
        boolean permits(ServerLevel level, CastAuthority authority, LivingEntity source, Vec3 position, @Nullable Entity target);
    }

    /** Registers a scope and its policy revision once during server initialization. */
    public static void register(Identifier scope, long revision, Resolver resolver) {
        BINDING.get().register(scope, revision, resolver);
    }

    /**
     * Issues a single-start grant after an explicit action by the present owner. Radius is limited
     * to 128 blocks and lifetime to 72000 ticks. Source loss always fails closed. Offline grants
     * additionally require the current scope resolver and an existing authoritative player record.
     */
    public static Optional<CastAuthority> grant(ServerPlayer owner, LivingEntity source, Identifier scope, int radius, long lifetimeTicks, boolean allowOffline) {
        return BINDING.get().grant(owner, source, scope, radius, lifetimeTicks, allowOffline);
    }

    /** Revokes this owner's grant and every continuation issued under it. */
    public static boolean revoke(ServerPlayer owner, CastAuthority authority) {
        return BINDING.get().revoke(owner, authority);
    }

    /** Binds Thaumaturge's server implementation. */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    public interface Bindings {
        void register(Identifier scope, long revision, Resolver resolver);

        Optional<CastAuthority> grant(ServerPlayer owner, LivingEntity source, Identifier scope, int radius, long lifetimeTicks, boolean allowOffline);

        boolean revoke(ServerPlayer owner, CastAuthority authority);
    }
}
