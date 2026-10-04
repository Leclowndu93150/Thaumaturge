package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.world.item.Item;

public final class GearWarp {
    private GearWarp() {}

    public static Item.Properties with(Item.Properties properties, int warp) {
        return properties.component(TCDataComponents.STACK_WARP.get(), warp);
    }
}
