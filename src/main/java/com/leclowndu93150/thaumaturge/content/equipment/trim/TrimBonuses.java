package com.leclowndu93150.thaumaturge.content.equipment.trim;

import com.leclowndu93150.thaumaturge.api.items.trim.TrimMaterialBonus;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public final class TrimBonuses {
    private static final TrimMaterialBonus NONE = new TrimMaterialBonus(0, 0);

    private TrimBonuses() {}

    public static TrimMaterialBonus of(ItemStack stack) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim == null) {
            return NONE;
        }
        TrimMaterialBonus bonus = trim.material().getData(TTDataMaps.TRIM_MATERIAL_BONUS);
        return bonus == null ? NONE : bonus;
    }
}
