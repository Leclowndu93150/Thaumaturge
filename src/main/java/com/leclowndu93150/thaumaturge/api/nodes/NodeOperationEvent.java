package com.leclowndu93150.thaumaturge.api.nodes;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public final class NodeOperationEvent extends Event implements ICancellableEvent {
    private final NodeOperationContext context;

    public NodeOperationEvent(NodeOperationContext context) {
        this.context = context;
    }

    public NodeOperationContext context() {
        return context;
    }
}
