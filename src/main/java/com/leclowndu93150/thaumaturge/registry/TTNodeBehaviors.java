package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorType;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTNodeBehaviors {
    public static final DeferredRegister<NodeBehaviorType<?>> BEHAVIORS = DeferredRegister.create(NodeBehaviorType.REGISTRY_KEY, TTIds.MODID);
    private static final Registry<NodeBehaviorType<?>> REGISTRY = BEHAVIORS.makeRegistry(builder -> builder.sync(false));

    private TTNodeBehaviors() {}

    public static Registry<NodeBehaviorType<?>> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        BEHAVIORS.register(modBus);
    }
}
