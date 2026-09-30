package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import net.minecraft.world.item.ItemStack;

public final class WandAccessBindings implements WandAccess.Bindings {

    @Override
    public WandVis getAllVis(ItemStack wand) {
        return WandVisHelper.getAllVis(wand);
    }

    @Override
    public void setAllVis(ItemStack wand, WandVis vis) {
        WandVisHelper.setAllVis(wand, vis);
    }
}
