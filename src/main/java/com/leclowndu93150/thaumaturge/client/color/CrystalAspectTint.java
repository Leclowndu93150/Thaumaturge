package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.ItemStack;

public final class CrystalAspectTint implements ItemColor {
    private static final int FALLBACK = 0xFFFFFF;

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        AspectInstance instance = stack.get(TTDataComponents.CRYSTAL_ASPECT.get());
        if (instance == null) {
            return ARGB32.opaque(FALLBACK);
        }
        return ARGB32.opaque(instance.aspect().value().color());
    }
}
