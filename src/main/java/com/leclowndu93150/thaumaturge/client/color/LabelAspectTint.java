package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.ItemStack;

public final class LabelAspectTint implements ItemColor {
    private static final int FALLBACK = 0xFFFFFF;
    private static final int WHITE = 0xFFFFFFFF;

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return WHITE;
        }
        ResourceKey<IAspect> aspect = stack.get(TTDataComponents.ASPECT_FILTER.get());
        ClientLevel level = Minecraft.getInstance().level;
        if (aspect == null || level == null) {
            return ARGB32.opaque(FALLBACK);
        }
        return level.registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(aspect)
                .map(holder -> ARGB32.opaque(holder.value().color()))
                .orElse(ARGB32.opaque(FALLBACK));
    }
}
