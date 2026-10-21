package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.nodes.NodeAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeChange;
import com.leclowndu93150.thaumaturge.api.nodes.NodeOperationContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeResult;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSnapshot;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSpec;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class NodeAccessBindings implements NodeAccess.Bindings {
    @Override
    public Optional<NodeSnapshot> inspect(ServerLevel level, BlockPos pos) {
        return NodeOperations.inspect(level, pos);
    }

    @Override
    public NodeResult tryUpdate(NodeOperationContext context, UUID expectedIdentity, long expectedRevision, NodeChange change, TransactionContext transaction) {
        return NodeOperations.update(context, expectedIdentity, expectedRevision, change, transaction);
    }

    @Override
    public NodeResult tryCreate(NodeOperationContext context, NodeSpec spec) {
        return NodeOperations.create(context, spec);
    }
}
