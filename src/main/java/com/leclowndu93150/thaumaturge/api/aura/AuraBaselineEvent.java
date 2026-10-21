package com.leclowndu93150.thaumaturge.api.aura;

import java.util.UUID;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired for quote and commit authorization. Offline delegation is denied initially: a protection
 * integration listening to canceled events must explicitly allow it after checking the owner's
 * access to the entire chunk.
 * Online operations may be denied by protection integrations in the same way as world changes.
 */
public final class AuraBaselineEvent extends Event implements ICancellableEvent {
    private final AuraBaseline.Context context;
    private final UUID owner;

    public AuraBaselineEvent(AuraBaseline.Context context, UUID owner, boolean offline) {
        this.context = context;
        this.owner = owner;
        setCanceled(offline);
    }

    public AuraBaseline.Context context() {
        return context;
    }

    public UUID owner() {
        return owner;
    }
}
