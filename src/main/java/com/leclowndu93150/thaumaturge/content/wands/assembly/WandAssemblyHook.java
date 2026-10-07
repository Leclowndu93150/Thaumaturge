package com.leclowndu93150.thaumaturge.content.wands.assembly;

import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.wands.IWandRodOnAssemble;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.world.item.ItemStack;

public final class WandAssemblyHook {
    private WandAssemblyHook() {}

    public static ItemStack apply(ItemStack result, IArcaneCraftingInput input) {
        WandParts parts = result.get(TTDataComponents.WAND_PARTS.get());
        if (parts == null) {
            return result;
        }
        IWandRodOnAssemble onAssemble = parts.rod().onAssemble();
        if (onAssemble != null) {
            onAssemble.onAssemble(result, input);
        }
        return result;
    }
}
