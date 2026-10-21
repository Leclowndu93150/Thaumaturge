package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.nodes.NodeAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeChange;
import com.leclowndu93150.thaumaturge.api.nodes.NodeOperationContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeOperationEvent;
import com.leclowndu93150.thaumaturge.api.nodes.NodeResult;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSnapshot;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSpec;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class NodeApiTests {
    private static final Identifier REASON = Identifier.fromNamespaceAndPath("thaumaturge", "api_test");
    private static final BlockPos POSITION = new BlockPos(2, 2, 2);

    private NodeApiTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("node_api/read_guards", 20, helper -> {
            BlockPos distant = new BlockPos(29_000_000, 100, 29_000_000);
            check(!helper.getLevel().hasChunkAt(distant), "Read test requires an unloaded chunk");
            check(NodeAccess.inspect(helper.getLevel(), distant).isEmpty(), "Unloaded node was visible");
            check(!helper.getLevel().hasChunkAt(distant), "Inspection loaded a chunk");
            NodeOperationContext context = create(helper);
            java.util.concurrent.atomic.AtomicBoolean hidden = new java.util.concurrent.atomic.AtomicBoolean();
            Thread thread = Thread.ofVirtual().start(() -> hidden.set(NodeAccess.inspect(context.level(), context.pos()).isEmpty()));
            try {
                thread.join();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
            check(hidden.get(), "Off-thread inspection accessed a live node");
            helper.succeed();
        });
        registrar.add("node_api/update_commit_and_stale", 20, helper -> {
            NodeOperationContext context = create(helper);
            NodeSnapshot original = NodeAccess.inspect(context.level(), context.pos()).orElseThrow();
            NodeChange change = new NodeChange(original.current(), original.base(), NodeType.PURE, original.modifier(), original.behavior());
            try (Transaction transaction = Transaction.openRoot()) {
                check(NodeAccess.tryUpdate(context, original.identity(), original.revision(), change, transaction) == NodeResult.SUCCESS, "Update was rejected");
                transaction.commit();
            }
            NodeSnapshot updated = NodeAccess.inspect(context.level(), context.pos()).orElseThrow();
            check(updated.identity().equals(original.identity()) && updated.type() == NodeType.PURE, "Committed update lost node identity or type");
            try (Transaction transaction = Transaction.openRoot()) {
                check(NodeAccess.tryUpdate(context, original.identity(), original.revision(), change, transaction) == NodeResult.STALE, "Stale revision was accepted");
            }
            helper.succeed();
        });
        registrar.add("node_api/rollback", 20, helper -> {
            NodeOperationContext context = create(helper);
            NodeSnapshot original = NodeAccess.inspect(context.level(), context.pos()).orElseThrow();
            try (Transaction transaction = Transaction.openRoot()) {
                NodeChange change = new NodeChange(AspectList.EMPTY, original.base(), NodeType.DARK, original.modifier(), original.behavior());
                check(NodeAccess.tryUpdate(context, original.identity(), original.revision(), change, transaction) == NodeResult.SUCCESS, "Update was rejected");
            }
            check(NodeAccess.inspect(context.level(), context.pos()).orElseThrow().equals(original), "Aborted transaction changed the node");
            helper.succeed();
        });
        registrar.add("node_api/denied_and_invalid", 20, helper -> {
            NodeOperationContext context = create(helper);
            NodeSnapshot original = NodeAccess.inspect(context.level(), context.pos()).orElseThrow();
            Consumer<NodeOperationEvent> deny = event -> {
                if (event.context().equals(context)) {
                    event.setCanceled(true);
                }
            };
            NeoForge.EVENT_BUS.addListener(deny);
            try (Transaction transaction = Transaction.openRoot()) {
                check(NodeAccess.tryUpdate(context, original.identity(), original.revision(), NodeChange.of(original), transaction) == NodeResult.DENIED, "Protection was ignored");
            } finally {
                NeoForge.EVENT_BUS.unregister(deny);
            }
            try (Transaction transaction = Transaction.openRoot()) {
                NodeChange invalid = new NodeChange(original.current(), AspectList.EMPTY, original.type(), original.modifier(), original.behavior());
                check(NodeAccess.tryUpdate(context, original.identity(), original.revision(), invalid, transaction) == NodeResult.INVALID, "Contents exceeded capacity");
            }
            check(NodeAccess.inspect(context.level(), context.pos()).orElseThrow().equals(original), "Rejected operation changed the node");
            helper.succeed();
        });
    }

    private static NodeOperationContext create(GameTestHelper helper) {
        NodeOperationContext context = NodeOperationContext.server(helper.getLevel(), helper.absolutePos(POSITION), REASON);
        AspectList aspects = AspectList.of(new AspectInstance(Aspects.resolve(helper.getLevel(), TTAspects.AER), 30));
        check(NodeAccess.tryCreate(context, new NodeSpec(aspects, aspects, NodeType.NORMAL, Optional.empty(), Optional.empty())) == NodeResult.SUCCESS, "Node creation failed");
        return context;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
