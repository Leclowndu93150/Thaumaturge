package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.leclowndu93150.thaumaturge.TTIds;
import net.neoforged.bus.api.IEventBus;

public final class DynamicTreesCompat {
    private DynamicTreesCompat() {}

    public static void init(IEventBus modBus) {
        NeoForgeRegistryHandler.setup(TTIds.MODID, modBus);
    }
}
