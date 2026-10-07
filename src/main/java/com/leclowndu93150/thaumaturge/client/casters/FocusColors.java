package com.leclowndu93150.thaumaturge.client.casters;

import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;

public final class FocusColors {
    private static final int WHITE = 0xFFFFFF;

    private FocusColors() {}

    public static int of(ItemStack focus) {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? WHITE : FocusItems.color(focus, level.registryAccess());
    }
}
