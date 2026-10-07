package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.ItemStack;

public final class NoteColorTint implements ItemColor {
    private static final int FALLBACK = 0x999999;
    private static final int WHITE = 0xFFFFFFFF;

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return WHITE;
        }
        ResearchNoteData data = stack.get(TTDataComponents.RESEARCH_NOTE.get());
        return ARGB32.opaque(data == null ? FALLBACK : data.color());
    }
}
