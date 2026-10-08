package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

public final class GolemMaterialTint implements ItemColor {
    private static final int OPAQUE = 0xFF000000;
    private static final int DEFAULT = 0xFFFFFFFF;

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        GolemProperties props = stack.get(TTDataComponents.GOLEM_PROPERTIES.get());
        if (props == null) {
            return DEFAULT;
        }
        return OPAQUE | props.material().itemColor();
    }
}
