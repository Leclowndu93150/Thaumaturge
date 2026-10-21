package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.nodes.CapturedNode;
import com.leclowndu93150.thaumaturge.api.nodes.INodeCaptureItem;
import com.leclowndu93150.thaumaturge.api.nodes.NodeChange;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehavior;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeOperationContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeResult;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSnapshot;
import com.leclowndu93150.thaumaturge.api.nodes.NodeTransfers;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class NodeTransferOperations implements NodeTransfers.Bindings {
    public NodeTransferOperations() {}

    @Override
    public Optional<CapturedNode> captured(ItemStack stack) {
        return Optional.ofNullable(stack.get(TTDataComponents.CAPTURED_NODE));
    }

    @Override
    public Optional<NodeSnapshot> prepare(NodeOperationContext context, UUID identity, long revision) {
        if (NodeOperations.guard(context) != NodeResult.SUCCESS || context.player().isEmpty()) {
            return Optional.empty();
        }
        BlockEntityNode node = NodeOperations.find(context.level(), context.pos());
        if (node == null || node instanceof BlockEntityJarNode || !reconcile(node)) {
            return Optional.empty();
        }
        NodeSnapshot snapshot = node.snapshot();
        return snapshot.identity().equals(identity) && snapshot.revision() == revision && !snapshot.energized() && !snapshot.locked() && !snapshot.busy() ? Optional.of(snapshot) : Optional.empty();
    }

    @Override
    public NodeResult capture(NodeOperationContext context, NodeSnapshot expected, int slot) {
        Optional<NodeSnapshot> prepared = prepare(context, expected.identity(), expected.revision());
        if (prepared.isEmpty()) {
            return NodeResult.STALE;
        }
        ServerPlayer player = context.player().orElseThrow();
        ItemStack vessel = vessel(player, slot);
        if (vessel.isEmpty() || vessel.has(TTDataComponents.CAPTURED_NODE)) {
            return NodeResult.INVALID;
        }
        NodeSnapshot snapshot = degrade(context.level(), prepared.get());
        if (!NodeOperations.valid(NodeChange.of(snapshot))) {
            return NodeResult.UNSUPPORTED;
        }
        CapturedNode captured = new CapturedNode(CapturedNode.VERSION, UUID.randomUUID(), snapshot);
        GlobalPos source = GlobalPos.of(context.level().dimension(), context.pos());
        try {
            NodeTransferJournal.write(context.level(), new NodeTransferJournal.Entry(captured, player.getUUID(), source, Optional.empty(), true));
        } catch (IOException exception) {
            return NodeResult.UNAVAILABLE;
        }
        if (!context.level().setBlock(context.pos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) {
            try {
                NodeTransferJournal.write(context.level(), new NodeTransferJournal.Entry(captured, player.getUUID(), source, Optional.of(source), true));
            } catch (IOException exception) {
                return NodeResult.UNAVAILABLE;
            }
            return NodeResult.UNAVAILABLE;
        }
        vessel.set(TTDataComponents.CAPTURED_NODE, captured);
        player.getInventory().setChanged();
        notifyCapture(context.level(), snapshot);
        return NodeResult.SUCCESS;
    }

    @Override
    public NodeResult release(NodeOperationContext context, int slot, BlockPos target) {
        NodeOperationContext destination = new NodeOperationContext(context.level(), target, context.player(), context.reason());
        NodeResult gate = NodeOperations.guardPlacement(destination);
        if (gate != NodeResult.SUCCESS || context.player().isEmpty()) {
            return gate == NodeResult.SUCCESS ? NodeResult.DENIED : gate;
        }
        ServerPlayer player = context.player().orElseThrow();
        ItemStack vessel = vessel(player, slot);
        CapturedNode captured = vessel.get(TTDataComponents.CAPTURED_NODE);
        if (captured == null || !context.level().getBlockState(target).isAir()) {
            return NodeResult.INVALID;
        }
        try {
            Optional<NodeTransferJournal.Entry> found = NodeTransferJournal.read(context.level(), captured.snapshot().identity());
            if (found.isEmpty() || !found.get().captured().transferId().equals(captured.transferId()) || found.get().target().isPresent()) {
                return NodeResult.STALE;
            }
            NodeTransferJournal.Entry entry = found.get();
            if (!NodeOperations.valid(NodeChange.of(entry.captured().snapshot()))) {
                return NodeResult.UNSUPPORTED;
            }
            NodeTransferJournal.Entry releasing = new NodeTransferJournal.Entry(entry.captured(), entry.owner(), entry.source(), Optional.of(GlobalPos.of(context.level().dimension(), target)), false);
            NodeTransferJournal.write(context.level(), releasing);
            if (!place(context.level(), target, entry.captured().snapshot())) {
                NodeTransferJournal.write(context.level(), entry);
                return NodeResult.UNAVAILABLE;
            }
            try {
                settle(context.level(), releasing);
            } catch (IOException exception) {
                Thaumaturge.LOGGER.error("Node release committed but its checkpoint needs recovery", exception);
            }
        } catch (IOException exception) {
            return NodeResult.UNAVAILABLE;
        }
        vessel.remove(TTDataComponents.CAPTURED_NODE);
        player.getInventory().setChanged();
        BlockEntityNode released = NodeOperations.find(context.level(), target);
        if (released != null) {
            released.behaviorEvent(NodeBehavior.Event.RELEASE);
        }
        return NodeResult.SUCCESS;
    }

    @Override
    public NodeResult recover(NodeOperationContext context, UUID identity, int slot) {
        if (NodeOperations.guard(context) != NodeResult.SUCCESS || context.player().isEmpty()) {
            return NodeResult.DENIED;
        }
        ServerPlayer player = context.player().orElseThrow();
        ItemStack vessel = vessel(player, slot);
        if (vessel.isEmpty() || vessel.has(TTDataComponents.CAPTURED_NODE)) {
            return NodeResult.INVALID;
        }
        try {
            Optional<NodeTransferJournal.Entry> found = NodeTransferJournal.read(context.level(), identity);
            if (found.isEmpty() || !found.get().owner().equals(player.getUUID())) {
                return NodeResult.STALE;
            }
            NodeTransferJournal.Entry entry = found.get();
            if (entry.target().isPresent()) {
                if (NodeOperations.guardPlacement(context) != NodeResult.SUCCESS) {
                    return NodeResult.DENIED;
                }
                GlobalPos target = entry.target().get();
                if (entry.settled() || !target.dimension().equals(context.level().dimension()) || !target.pos().equals(context.pos())) {
                    return NodeResult.STALE;
                }
                BlockEntityNode node = context.level().getBlockEntity(target.pos()) instanceof BlockEntityNode existing ? existing : null;
                if (node == null) {
                    if (!context.level().getBlockState(target.pos()).isAir() || !place(context.level(), target.pos(), entry.captured().snapshot())) {
                        return NodeResult.UNAVAILABLE;
                    }
                } else if (!node.snapshot().identity().equals(identity)) {
                    return NodeResult.STALE;
                }
                settle(context.level(), entry);
                return NodeResult.SUCCESS;
            }
            vessel.set(TTDataComponents.CAPTURED_NODE, found.get().captured());
            player.getInventory().setChanged();
            return NodeResult.SUCCESS;
        } catch (IOException exception) {
            return NodeResult.UNAVAILABLE;
        }
    }

    public static boolean reconcile(BlockEntityNode node) {
        if (!(node.getLevel() instanceof ServerLevel level)) {
            return true;
        }
        try {
            Optional<NodeTransferJournal.Entry> found = NodeTransferJournal.read(level, node.snapshot().identity());
            if (found.isEmpty() && node.hasLegacyIdentity()) {
                found = NodeTransferJournal.legacySource(level, node.getBlockPos());
            }
            if (found.isEmpty()) {
                return true;
            }
            NodeTransferJournal.Entry entry = found.get();
            if (entry.target().filter(target -> target.dimension().equals(level.dimension()) && target.pos().equals(node.getBlockPos())).isPresent()) {
                if (node.hasLegacyIdentity() && !node.snapshot().identity().equals(entry.captured().snapshot().identity())) {
                    node.restoreSnapshot(entry.captured().snapshot());
                    node.setChanged();
                }
                if (!entry.settled()) {
                    if (!node.hasPersistedIdentity()) {
                        return false;
                    }
                    NodeTransferJournal.write(level, new NodeTransferJournal.Entry(entry.captured(), entry.owner(), entry.source(), entry.target(), true));
                }
                return true;
            }
            level.setBlock(node.getBlockPos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return false;
        } catch (IOException exception) {
            return false;
        }
    }

    private static boolean place(ServerLevel level, BlockPos pos, NodeSnapshot snapshot) {
        if (!level.setBlock(pos, TTBlocks.NODE.get().defaultBlockState(), Block.UPDATE_ALL)) {
            return false;
        }
        if (!(level.getBlockEntity(pos) instanceof BlockEntityNode node)) {
            return false;
        }
        node.restoreSnapshot(snapshot);
        node.suspendTransfer();
        node.setChanged();
        level.sendBlockUpdated(pos, node.getBlockState(), node.getBlockState(), Block.UPDATE_ALL);
        return true;
    }

    private static void settle(ServerLevel level, NodeTransferJournal.Entry entry) throws IOException {
        level.getChunkSource().save(true);
        NodeTransferJournal.write(level, new NodeTransferJournal.Entry(entry.captured(), entry.owner(), entry.source(), entry.target(), true));
        entry.target().ifPresent(target -> {
            if (level.getBlockEntity(target.pos()) instanceof BlockEntityNode node && node.snapshot().identity().equals(entry.captured().snapshot().identity())) {
                node.resumeTransfer();
            }
        });
    }

    private static void notifyCapture(ServerLevel level, NodeSnapshot snapshot) {
        snapshot.behavior().ifPresent(state -> new NodeBehaviorRunner().invoke(new NodeBehaviorContext() {
            @Override
            public NodeSnapshot snapshot() {
                return snapshot;
            }

            @Override
            public RandomSource random() {
                return level.getRandom();
            }

            @Override
            public NodeResult update(NodeChange change) {
                return NodeResult.UNSUPPORTED;
            }
        }, state, NodeBehavior.Event.CAPTURE));
    }

    private static ItemStack vessel(ServerPlayer player, int slot) {
        if (slot < 0 || slot >= player.getInventory().getContainerSize()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = player.getInventory().getItem(slot);
        return stack.getCount() == 1 && stack.getItem() instanceof INodeCaptureItem ? stack : ItemStack.EMPTY;
    }

    private static NodeSnapshot degrade(ServerLevel level, NodeSnapshot snapshot) {
        Optional<NodeModifier> modifier = snapshot.modifier();
        if (level.getRandom().nextFloat() < 0.75F) {
            modifier = modifier.isEmpty() ? Optional.of(NodeModifier.PALE) : switch (modifier.get()) {
                case BRIGHT -> Optional.empty();
                case PALE, FADING -> Optional.of(NodeModifier.FADING);
            };
        }
        return new NodeSnapshot(snapshot.identity(), snapshot.revision(), snapshot.current(), snapshot.base(), snapshot.originalBase(), snapshot.type(), modifier, false, false, false,
                snapshot.behavior());
    }
}
