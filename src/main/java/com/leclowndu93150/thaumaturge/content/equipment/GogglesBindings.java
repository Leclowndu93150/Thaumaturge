package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.registry.TCAttributes;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

public final class GogglesBindings implements GogglesAccess.Bindings {
    @Override
    public Holder<Attribute> visDiscount() {
        return TCAttributes.VIS_DISCOUNT;
    }

    @Override
    public boolean isRevealing(ItemStack stack) {
        return stack.has(TCDataComponents.GOGGLES_UPGRADE.get());
    }
}
