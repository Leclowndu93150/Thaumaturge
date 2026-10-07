package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

public final class GogglesBindings implements GogglesAccess.Bindings {
    @Override
    public Holder<Attribute> visDiscount() {
        return TTAttributes.VIS_DISCOUNT;
    }

    @Override
    public boolean isRevealing(ItemStack stack) {
        return stack.has(TTDataComponents.GOGGLES_UPGRADE.get());
    }
}
