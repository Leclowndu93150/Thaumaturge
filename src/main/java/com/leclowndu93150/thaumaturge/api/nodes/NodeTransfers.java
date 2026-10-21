package com.leclowndu93150.thaumaturge.api.nodes;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** Server-thread node transfers using single-item {@link INodeCaptureItem} inventory slots. */
public final class NodeTransfers {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("NodeTransfers");
    private NodeTransfers() {}

    /** Installs Thaumaturge's implementation once during mod construction. */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /** Reads the server-owned carrier payload without authorizing release. */
    public static Optional<CapturedNode> captured(ItemStack stack) {
        return BINDING.get().captured(stack);
    }

    /** Checks a loaded source without reserving it; capture rechecks the identity and revision. */
    public static Optional<NodeSnapshot> prepareCapture(NodeOperationContext context, UUID identity, long revision) {
        return BINDING.get().prepare(context, identity, revision);
    }

    /** Moves a node into an empty vessel, applying the standard modifier degradation rule. */
    public static NodeResult capture(NodeOperationContext context, NodeSnapshot expected, int inventorySlot) {
        return BINDING.get().capture(context, expected, inventorySlot);
    }

    /** Releases into loaded air in the context dimension, retaining the empty vessel. */
    public static NodeResult release(NodeOperationContext context, int inventorySlot, BlockPos target) {
        return BINDING.get().release(context, inventorySlot, target);
    }

    /**
     * Recovers an interrupted transfer for its original captor. An unreleased node can be restored
     * to an empty vessel; an unfinished release is recovered at the context position. Recovered
     * carriers share one journal token, so only one carrier can release the node.
     */
    public static NodeResult recover(NodeOperationContext context, UUID identity, int inventorySlot) {
        return BINDING.get().recover(context, identity, inventorySlot);
    }

    /** Implementation hook supplied by Thaumaturge. */
    public interface Bindings {
        Optional<CapturedNode> captured(ItemStack stack);
        Optional<NodeSnapshot> prepare(NodeOperationContext context, UUID identity, long revision);
        NodeResult capture(NodeOperationContext context, NodeSnapshot expected, int inventorySlot);
        NodeResult release(NodeOperationContext context, int inventorySlot, BlockPos target);
        NodeResult recover(NodeOperationContext context, UUID identity, int inventorySlot);
    }
}
