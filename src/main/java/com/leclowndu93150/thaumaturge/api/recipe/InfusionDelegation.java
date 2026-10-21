package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Server-owned, revocable authority for an infusion device acting for its owner. */
public final class InfusionDelegation {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("InfusionDelegation");

    private InfusionDelegation() {}

    /**
     * Implemented by a device block entity; ownership must come from its server state. Mint a new
     * device ID on each placement and ownership transfer, retain it across chunk saves, and never
     * copy it into dropped items or cloned devices. A grant belongs to that placement only.
     */
    public interface Device {
        UUID infusionOwner();

        UUID infusionDeviceId();

        Identifier infusionAddon();
    }

    /** An opaque reference to authority stored by the server, never a knowledge snapshot. */
    public record Handle(UUID id) {
        public Handle {
            Objects.requireNonNull(id, "id");
        }
    }

    /** The loaded device and matrix to which a grant applies. */
    public record Context(InfusionMatrixContext matrix, BlockPos devicePosition, Handle handle) {
        public Context {
            Objects.requireNonNull(matrix, "matrix");
            devicePosition = Objects.requireNonNull(devicePosition, "devicePosition").immutable();
            Objects.requireNonNull(handle, "handle");
        }
    }

    /**
     * Issues authority after an explicit action by the present owner. The device and matrix must
     * be loaded in the owner's level. The nonempty recipe allowlist and expiry are immutable.
     */
    public static Optional<Handle> grant(ServerPlayer owner, BlockPos devicePosition, BlockPos matrixPosition, Set<ResourceKey<Recipe<?>>> recipes, long lifetimeTicks) {
        return BINDING.get().grant(owner, devicePosition, matrixPosition, Set.copyOf(recipes), lifetimeTicks);
    }

    /** Issues a grant carrying the surcharge approved by a registered automation policy. */
    public static Optional<Handle> grant(ServerPlayer owner, BlockPos devicePosition, BlockPos matrixPosition, Set<ResourceKey<Recipe<?>>> recipes, long lifetimeTicks, Identifier policy) {
        return BINDING.get().grant(owner, devicePosition, matrixPosition, Set.copyOf(recipes), lifetimeTicks, Objects.requireNonNull(policy, "policy"));
    }

    /** Revokes a grant owned by this present player; persisted revocations survive reloads. */
    public static boolean revoke(ServerPlayer owner, Handle handle) {
        return BINDING.get().revoke(owner, handle);
    }

    /**
     * Sets server policy. Delegation starts disabled. Changing policy invalidates every existing
     * grant; administrative callers must authorize this operation themselves.
     */
    public static void configure(MinecraftServer server, boolean enabled, boolean allowOffline) {
        BINDING.get().configure(server, enabled, allowOffline);
    }

    /** Binds Thaumaturge's implementation during initialization. */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /** Protection integrations may deny both issuance and every subsequent use. */
    public static final class PermissionCheck extends Event implements ICancellableEvent {
        private final InfusionMatrixContext matrix;
        private final BlockPos devicePosition;

        public PermissionCheck(InfusionMatrixContext matrix, BlockPos devicePosition) {
            this.matrix = matrix;
            this.devicePosition = devicePosition.immutable();
        }

        public InfusionMatrixContext matrix() {
            return matrix;
        }

        public BlockPos devicePosition() {
            return devicePosition;
        }
    }

    /** Implementation hook supplied by Thaumaturge. */
    public interface Bindings {
        Optional<Handle> grant(ServerPlayer owner, BlockPos devicePosition, BlockPos matrixPosition, Set<ResourceKey<Recipe<?>>> recipes, long lifetimeTicks);

        Optional<Handle> grant(ServerPlayer owner, BlockPos devicePosition, BlockPos matrixPosition, Set<ResourceKey<Recipe<?>>> recipes, long lifetimeTicks, Identifier policy);

        boolean revoke(ServerPlayer owner, Handle handle);

        void configure(MinecraftServer server, boolean enabled, boolean allowOffline);
    }
}
