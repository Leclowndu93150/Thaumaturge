package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.client.casters.FocusColors;
import com.leclowndu93150.thaumaturge.content.casters.SocketedFocus;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

public final class FocusColorTint implements ItemColor {
    private static final int OPAQUE = 0xFF000000;

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        SocketedFocus socketed = stack.get(TTDataComponents.SOCKETED_FOCUS.get());
        ItemStack focus = socketed != null ? socketed.focus() : stack;
        return OPAQUE | FocusColors.of(focus);
    }
}
