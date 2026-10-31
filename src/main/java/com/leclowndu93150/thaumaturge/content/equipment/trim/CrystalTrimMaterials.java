package com.leclowndu93150.thaumaturge.content.equipment.trim;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.TrimMaterial;

public final class CrystalTrimMaterials {
    private CrystalTrimMaterials() {}

    public static boolean isCrystal(ItemStack stack) {
        return stack.is(TTItems.ESSENTIA_CRYSTAL.get());
    }

    public static Optional<Holder.Reference<TrimMaterial>> find(HolderLookup.Provider registries, ItemStack stack) {
        Holder<IAspect> aspect = ItemEssentiaCrystal.aspectOf(stack);
        Holder<TrimMaterial> material = aspect == null ? null : aspect.getData(TTTrimMaterials.CRYSTAL_MATERIAL);
        if (material == null) {
            return Optional.empty();
        }
        return material.unwrapKey().flatMap(registries.lookupOrThrow(Registries.TRIM_MATERIAL)::get);
    }
}
