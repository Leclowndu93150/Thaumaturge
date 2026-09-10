package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/** Mod-bus event for registering host-aware arcane workbench aura sources. */
public final class RegisterWorkbenchAuraSourcesEvent extends Event implements IModBusEvent {
    private final List<IWorkbenchAuraSource> sources = new ArrayList<>();

    public void register(IWorkbenchAuraSource source) {
        sources.add(source);
    }

    public List<IWorkbenchAuraSource> sources() {
        return List.copyOf(sources);
    }
}
