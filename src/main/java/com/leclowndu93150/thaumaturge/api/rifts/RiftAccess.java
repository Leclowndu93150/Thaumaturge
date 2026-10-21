package com.leclowndu93150.thaumaturge.api.rifts;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

public final class RiftAccess {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("RiftAccess");

    private RiftAccess() {}

    public static Optional<RiftSnapshot> inspect(ServerLevel level, UUID identity) {
        return BINDING.get().inspect(level, identity);
    }

    /** Queries at most 64 results in a box no wider than 64 blocks on any axis; never loads chunks. */
    public static List<RiftSnapshot> query(ServerLevel level, AABB bounds, int limit) {
        return BINDING.get().query(level, bounds, limit);
    }

    /** Reserves a bounded mutation and owns the transaction used for its payment. */
    public static RiftMutation prepareUpdate(RiftOperationContext context, RiftSnapshot expected, RiftChange change) {
        return BINDING.get().prepareUpdate(context, expected, change);
    }

    /** Checks creation and payment without spawning a rift or withdrawing flux. */
    public static RiftResult simulateCreation(RiftOperationContext context, RiftSpec spec) {
        return BINDING.get().simulateCreation(context, spec);
    }

    public static RiftResult tryCreate(RiftOperationContext context, RiftSpec spec) {
        return BINDING.get().tryCreate(context, spec);
    }

    /** Implementation hook installed by Thaumaturge. */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    public interface Bindings {
        Optional<RiftSnapshot> inspect(ServerLevel level, UUID identity);
        List<RiftSnapshot> query(ServerLevel level, AABB bounds, int limit);
        RiftMutation prepareUpdate(RiftOperationContext context, RiftSnapshot expected, RiftChange change);
        RiftResult simulateCreation(RiftOperationContext context, RiftSpec spec);
        RiftResult tryCreate(RiftOperationContext context, RiftSpec spec);
    }
}
