package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public final class AspectFilterTint implements ItemColor {
    private static final int OPAQUE = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private final int defaultColor;

    public AspectFilterTint(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return WHITE;
        }
        if (tintIndex == 1) {
            return OPAQUE | defaultColor;
        }
        ResourceKey<IAspect> aspect = stack.get(TTDataComponents.ASPECT_FILTER.get());
        ClientLevel level = Minecraft.getInstance().level;
        if (aspect != null && level != null) {
            return level.registryAccess()
                    .lookupOrThrow(IAspect.REGISTRY_KEY)
                    .get(aspect)
                    .map(holder -> OPAQUE | holder.value().color())
                    .orElse(OPAQUE | defaultColor);
        }
        return OPAQUE | defaultColor;
    }
}
