package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehavior;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorState;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorType;
import com.leclowndu93150.thaumaturge.registry.TTNodeBehaviors;

public final class NodeBehaviorRunner {
    private boolean disabled;

    public void reset() {
        disabled = false;
    }

    public Result invoke(NodeBehaviorContext context, NodeBehaviorState state, NodeBehavior.Event event) {
        if (disabled) {
            return new Result(state, false);
        }
        NodeBehaviorType<?> type = TTNodeBehaviors.registry().getValue(state.id());
        if (type == null || type.version() != state.version()) {
            disabled = true;
            Thaumaturge.LOGGER.warn("Node behavior {} version {} is unavailable; preserving its state", state.id(), state.version());
            return new Result(state, false);
        }
        try {
            return new Result(invoke(type, context, state, event), true);
        } catch (RuntimeException exception) {
            disabled = true;
            Thaumaturge.LOGGER.warn("Node behavior {} failed; preserving its state and disabling callbacks", state.id(), exception);
            return new Result(state, false);
        }
    }

    private static <S> NodeBehaviorState invoke(NodeBehaviorType<S> type, NodeBehaviorContext context, NodeBehaviorState state, NodeBehavior.Event event) {
        S decoded = type.decode(state).getOrThrow();
        S updated = type.behavior().apply(context, decoded, event);
        return type.encode(state.id(), updated).getOrThrow();
    }

    public record Result(NodeBehaviorState state, boolean success) {
    }
}
