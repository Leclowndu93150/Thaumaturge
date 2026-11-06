package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;

public final class ArmorTrimColors {
    private static final int UNTINTED = -1;

    private ArmorTrimColors() {}

    public static int of(Holder<TrimMaterial> material) {
        TrimMaterial value = material.value();
        if (!value.assetName().equals(TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET)) {
            return UNTINTED;
        }
        TextColor color = value.description().getStyle().getColor();
        return color == null ? UNTINTED : FastColor.ARGB32.opaque(color.getValue());
    }

    public static int of(ItemStack stack) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        return trim == null ? UNTINTED : of(trim.material());
    }
}
