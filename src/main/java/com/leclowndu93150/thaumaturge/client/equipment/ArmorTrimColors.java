package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public final class ArmorTrimColors {
    private static final int UNTINTED = -1;

    private ArmorTrimColors() {}

    public static int of(Holder<TrimMaterial> material) {
        TrimMaterial value = material.value();
        if (!value.assets().base().suffix().equals(TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET)) {
            return UNTINTED;
        }
        TextColor color = value.description().getStyle().getColor();
        return color == null ? UNTINTED : ARGB.opaque(color.getValue());
    }
}
