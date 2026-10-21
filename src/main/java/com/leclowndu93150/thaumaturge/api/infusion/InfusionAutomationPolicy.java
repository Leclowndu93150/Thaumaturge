package com.leclowndu93150.thaumaturge.api.infusion;

import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-owned automation policies registered by addons during initialization. A policy derives
 * its surcharge from authoritative state; packet handlers must never accept a surcharge from a client.
 */
public final class InfusionAutomationPolicy {
    public static final int MAX_EXTRA_INSTABILITY = 100;
    private static final Map<Identifier, Policy> POLICIES = new ConcurrentHashMap<>();

    private InfusionAutomationPolicy() {}

    @FunctionalInterface
    public interface Policy {
        /** Returns a surcharge in {@code [0, 100]}, or a negative value to deny this start. */
        int extraInstability(InfusionMatrixContext context, ServerPlayer player);
    }

    /** Registers a trusted server policy. Existing policies cannot be replaced. */
    public static void register(Identifier id, Policy policy) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(policy, "policy");
        if (POLICIES.putIfAbsent(id, policy) != null) {
            throw new IllegalArgumentException("Infusion automation policy already registered: " + id);
        }
    }

    /** Resolves a policy on the server thread and rejects unknown or out-of-range results. */
    public static int resolve(Identifier id, InfusionMatrixContext context, ServerPlayer player) {
        if (!context.level().getServer().isSameThread()) {
            return -1;
        }
        Policy policy = POLICIES.get(id);
        if (policy == null) {
            return -1;
        }
        int surcharge = policy.extraInstability(context, player);
        return surcharge >= 0 && surcharge <= MAX_EXTRA_INSTABILITY ? surcharge : -1;
    }
}
