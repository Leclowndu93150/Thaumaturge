package com.leclowndu93150.thaumaturge.content.equipment.trim;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import org.jspecify.annotations.Nullable;

public final class CrystalTrimMaterials {
    private CrystalTrimMaterials() {}

    public static @Nullable Holder<TrimMaterial> of(ItemStack stack) {
        Holder<IAspect> aspect = ItemEssentiaCrystal.aspectOf(stack);
        return aspect == null ? null : aspect.getData(TTTrimMaterials.CRYSTAL_MATERIAL);
    }
}
