package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.ItemStack;

public final class AspectColorTint implements ItemColor {
    private static final int FALLBACK = 0xFFFFFF;
    private static final int WHITE = 0xFFFFFFFF;

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return WHITE;
        }
        AspectList list = stack.get(TTDataComponents.ASPECTS.get());
        if (list == null || list.isEmpty()) {
            return ARGB32.opaque(FALLBACK);
        }
        AspectInstance first = list.entries().get(0);
        return ARGB32.opaque(first.aspect().value().color());
    }
}
