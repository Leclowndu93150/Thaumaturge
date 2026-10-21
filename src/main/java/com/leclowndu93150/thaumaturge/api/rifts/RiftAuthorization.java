package com.leclowndu93150.thaumaturge.api.rifts;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Revocable owner consent scoped exclusively to rift operations at a concrete machine. */
public final class RiftAuthorization {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("RiftAuthorization");

    private RiftAuthorization() {}

    public enum Action {
        CREATE, MUTATE, MOVE, CAPTURE, RELEASE
    }

    public record Handle(UUID id) {
        public Handle {
            Objects.requireNonNull(id, "id");
        }
    }

    /** Placement identity must change when the machine is replaced or its owner changes. */
    public record Machine(UUID owner, UUID instance) {
        public Machine {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(instance, "instance");
        }
    }

    @FunctionalInterface
    public interface Resolver {
        /** Inspects the actual loaded machine, returning empty when its authority cannot be verified. */
        Optional<Machine> resolve(ServerLevel level, BlockPos position);

        /** Explicit offline claim check over the complete affected volume; the default denies access. */
        default boolean authorizeOffline(ServerLevel level, BlockPos position, Machine machine, Action action, AABB volume) {
            return false;
        }
    }

    /** Register trusted machine implementations during initialization, before any server use. */
    public static void register(Identifier id, Resolver resolver, boolean supportsOffline) {
        BINDING.get().register(id, resolver, supportsOffline);
    }

    /** Called following explicit owner consent; radius is at most 64 blocks, lifetime at most seven game days. */
    public static Optional<Handle> grant(ServerPlayer owner, BlockPos machine, Identifier resolver, Set<Action> actions, int radius, long lifetimeTicks) {
        return BINDING.get().grant(owner, machine, resolver, Set.copyOf(actions), radius, lifetimeTicks);
    }

    public static boolean revoke(ServerPlayer owner, Handle handle) {
        return BINDING.get().revoke(owner, handle);
    }

    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /** Fired at issuance and each use, including offline use, for claim/protection integrations. */
    public static final class PermissionCheck extends Event implements ICancellableEvent {
        private final RiftOperationContext context;
        private final Action action;
        private final BlockPos target;
        private final UUID owner;

        public PermissionCheck(RiftOperationContext context, Action action, BlockPos target, UUID owner) {
            this.context = context;
            this.action = action;
            this.target = target.immutable();
            this.owner = Objects.requireNonNull(owner, "owner");
        }

        public RiftOperationContext context() {
            return context;
        }
        public Action action() {
            return action;
        }
        public BlockPos target() {
            return target;
        }
        public UUID owner() {
            return owner;
        }
    }

    public interface Bindings {
        void register(Identifier id, Resolver resolver, boolean supportsOffline);
        Optional<Handle> grant(ServerPlayer owner, BlockPos machine, Identifier resolver, Set<Action> actions, int radius, long lifetimeTicks);
        boolean revoke(ServerPlayer owner, Handle handle);
    }
}
