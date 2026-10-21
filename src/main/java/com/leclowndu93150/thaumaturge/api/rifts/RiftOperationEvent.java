package com.leclowndu93150.thaumaturge.api.rifts;

import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Additional protection gate for the full affected source and destination volumes. */
public final class RiftOperationEvent extends Event implements ICancellableEvent {
    private final RiftOperationContext context;
    private final RiftAuthorization.Action action;
    private final AABB source;
    private final AABB destination;

    public RiftOperationEvent(RiftOperationContext context, RiftAuthorization.Action action, AABB source, AABB destination) {
        this.context = context;
        this.action = action;
        this.source = source;
        this.destination = destination;
    }

    public RiftOperationContext context() {
        return context;
    }
    public RiftAuthorization.Action action() {
        return action;
    }
    public AABB source() {
        return source;
    }
    public AABB destination() {
        return destination;
    }
}
