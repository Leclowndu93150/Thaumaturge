package com.leclowndu93150.thaumaturge.api.nodes;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Server-thread access to loaded nodes. Snapshots and revisions are observations, not permissions. */
public final class NodeAccess {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("NodeAccess");

    private NodeAccess() {}

    /** Reads a loaded node without loading chunks or changing its state. */
    public static Optional<NodeSnapshot> inspect(ServerLevel level, BlockPos pos) {
        return BINDING.get().inspect(level, pos);
    }

    /**
     * Stages one bounded update in the caller's resource transaction. Close the transaction during
     * the same server call, without yielding or changing the world between staging and commit.
     * Abort the transaction when any payment or operation fails. Notifications occur on root commit.
     */
    public static NodeResult tryUpdate(NodeOperationContext context, UUID expectedIdentity, long expectedRevision, NodeChange change, TransactionContext transaction) {
        return BINDING.get().tryUpdate(context, expectedIdentity, expectedRevision, change, transaction);
    }

    /** Creates a node for a creative player or an explicitly authorized server operation. */
    public static NodeResult tryCreate(NodeOperationContext context, NodeSpec spec) {
        return BINDING.get().tryCreate(context, spec);
    }

    /** Implementation hook installed once by Thaumaturge; addons must not call this. */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    public interface Bindings {
        Optional<NodeSnapshot> inspect(ServerLevel level, BlockPos pos);

        NodeResult tryUpdate(NodeOperationContext context, UUID expectedIdentity, long expectedRevision, NodeChange change, TransactionContext transaction);

        NodeResult tryCreate(NodeOperationContext context, NodeSpec spec);
    }
}
