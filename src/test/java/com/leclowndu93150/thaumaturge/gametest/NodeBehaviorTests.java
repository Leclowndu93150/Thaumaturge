package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.nodes.NodeAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehavior;
import com.leclowndu93150.thaumaturge.api.nodes.NodeChange;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorState;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorType;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeOperationContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeResult;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSpec;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@EventBusSubscriber(modid = TTIds.MODID)
public final class NodeBehaviorTests {
    private static final Identifier COUNTER_ID = TTIds.rl("test_counter_behavior");
    private static final Identifier FAILING_ID = TTIds.rl("test_failing_behavior");
    private static final AtomicInteger FAILURES = new AtomicInteger();
    private static final NodeBehaviorType<Integer> COUNTER = new NodeBehaviorType<>(1, Codec.INT, (context, state, event) -> {
        if (event == NodeBehavior.Event.ATTACH) {
            var snapshot = context.snapshot();
            context.update(new NodeChange(snapshot.current(), snapshot.base(), snapshot.type(), Optional.of(NodeModifier.BRIGHT), snapshot.behavior()));
            return state + 10;
        }
        return switch (event) {
            case LOAD -> state + 100;
            case TICK -> state + 1;
            default -> state;
        };
    });
    private static final NodeBehaviorType<Integer> FAILING = new NodeBehaviorType<>(1, Codec.INT, (context, state, event) -> {
        FAILURES.incrementAndGet();
        var snapshot = context.snapshot();
        context.update(new NodeChange(snapshot.current(), snapshot.base(), snapshot.type(), Optional.of(NodeModifier.PALE), snapshot.behavior()));
        throw new IllegalStateException("Intentional behavior failure");
    });

    private NodeBehaviorTests() {}

    @SubscribeEvent
    public static void registerTypes(RegisterEvent event) {
        event.register(NodeBehaviorType.REGISTRY_KEY, registration -> {
            registration.register(COUNTER_ID, COUNTER);
            registration.register(FAILING_ID, FAILING);
        });
    }

    public static void register(TTTestRegistrar registrar) {
        registrar.add("node/registered_behavior_lifecycle", 20, helper -> {
            BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
            NodeSpec spec = new NodeSpec(AspectList.EMPTY, AspectList.EMPTY, NodeType.NORMAL, Optional.empty(), Optional.empty());
            if (NodeAccess.tryCreate(NodeOperationContext.server(helper.getLevel(), pos, COUNTER_ID), spec) != NodeResult.SUCCESS) {
                helper.fail("Could not create behavior node");
                return;
            }
            BlockEntityNode node = (BlockEntityNode) helper.getLevel().getBlockEntity(pos);
            var before = node.snapshot();
            NodeChange attach = new NodeChange(before.current(), before.base(), before.type(), before.modifier(), Optional.of(COUNTER.encode(COUNTER_ID, 0).getOrThrow()));
            try (Transaction transaction = Transaction.openRoot()) {
                if (NodeAccess.tryUpdate(NodeOperationContext.server(helper.getLevel(), pos, COUNTER_ID), before.identity(), before.revision(), attach, transaction) != NodeResult.SUCCESS) {
                    helper.fail("Could not attach registered behavior");
                    return;
                }
                transaction.commit();
            }
            if (node.trait() != NodeModifier.BRIGHT || COUNTER.decode(node.snapshot().behavior().orElseThrow()).getOrThrow() != 10) {
                helper.fail("Attach callback did not commit state and node update together");
                return;
            }
            node.behaviorEvent(NodeBehavior.Event.LOAD);
            node.behaviorEvent(NodeBehavior.Event.TICK);
            if (COUNTER.decode(node.snapshot().behavior().orElseThrow()).getOrThrow() != 111) {
                helper.fail("Registered behavior did not receive load and tick callbacks");
                return;
            }
            helper.succeed();
        });

        registrar.add("node/failed_behavior_rolls_back_and_stops", 20, helper -> {
            BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
            NodeBehaviorState state = FAILING.encode(FAILING_ID, 9).getOrThrow();
            int failuresBefore = FAILURES.get();
            NodeSpec spec = new NodeSpec(AspectList.EMPTY, AspectList.EMPTY, NodeType.NORMAL, Optional.empty(), Optional.of(state));
            if (NodeAccess.tryCreate(NodeOperationContext.server(helper.getLevel(), pos, FAILING_ID), spec) != NodeResult.SUCCESS) {
                helper.fail("Could not create behavior node");
                return;
            }
            BlockEntityNode node = (BlockEntityNode) helper.getLevel().getBlockEntity(pos);
            node.behaviorEvent(NodeBehavior.Event.TICK);
            node.behaviorEvent(NodeBehavior.Event.TICK);
            if (node.trait() != null || !node.snapshot().behavior().orElseThrow().equals(state) || FAILURES.get() != failuresBefore + 1) {
                helper.fail("Failed behavior committed an update, lost state, or continued ticking");
                return;
            }
            helper.succeed();
        });

        registrar.add("node/behavior_state_roundtrip", 20, helper -> {
            NodeBehaviorType<Integer> type = new NodeBehaviorType<>(2, Codec.INT, (context, state, event) -> state);
            NodeBehaviorState state = type.encode(TTIds.rl("test_behavior"), 37).getOrThrow();
            NodeBehaviorState restored = NodeBehaviorState.CODEC.parse(JsonOps.INSTANCE, NodeBehaviorState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow()).getOrThrow();
            if (!restored.equals(state) || type.decode(restored).getOrThrow() != 37) {
                helper.fail("Node behavior state did not survive codec roundtrip");
                return;
            }
            helper.succeed();
        });

        registrar.add("node/unknown_behavior_state_preserved", 20, helper -> {
            NodeBehaviorState state = new NodeBehaviorState(TTIds.rl("missing_behavior"), 123, "opaque future data");
            NodeBehaviorState restored = NodeBehaviorState.CODEC.parse(JsonOps.INSTANCE, NodeBehaviorState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow()).getOrThrow();
            NodeBehaviorType<Integer> type = new NodeBehaviorType<>(1, Codec.INT, (context, value, event) -> value);
            if (!restored.equals(state) || type.decode(restored).isSuccess()) {
                helper.fail("Unknown behavior state was lost or decoded with an incompatible version");
                return;
            }
            BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
            NodeSpec spec = new NodeSpec(AspectList.EMPTY, AspectList.EMPTY, NodeType.NORMAL, Optional.empty(), Optional.of(state));
            if (NodeAccess.tryCreate(NodeOperationContext.server(helper.getLevel(), pos, state.id()), spec) != NodeResult.SUCCESS) {
                helper.fail("Could not restore node with unavailable behavior");
                return;
            }
            BlockEntityNode node = (BlockEntityNode) helper.getLevel().getBlockEntity(pos);
            node.behaviorEvent(NodeBehavior.Event.LOAD);
            node.behaviorEvent(NodeBehavior.Event.TICK);
            if (!node.snapshot().behavior().orElseThrow().equals(state)) {
                helper.fail("Unavailable behavior did not preserve opaque state across callbacks");
                return;
            }
            helper.succeed();
        });

        registrar.add("node/behavior_state_size_limit", 20, helper -> {
            NodeBehaviorType<String> type = new NodeBehaviorType<>(1, Codec.STRING, (context, value, event) -> value);
            if (type.encode(TTIds.rl("test_behavior"), "x".repeat(NodeBehaviorState.MAX_DATA_BYTES)).isSuccess()) {
                helper.fail("Oversized behavior state was accepted");
                return;
            }
            if (type.decode(new NodeBehaviorState(TTIds.rl("test_behavior"), 1, "{")).isSuccess()) {
                helper.fail("Malformed behavior state was accepted");
                return;
            }
            helper.succeed();
        });
    }
}
