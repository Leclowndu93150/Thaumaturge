package com.leclowndu93150.thaumaturge.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public record TTGauge(ResourceLocation sheet, int sheetSize, int u, int v, int width, int height) {
    public void renderFull(GuiGraphics graphics, int x, int y) {
        renderRising(graphics, x, y, height);
    }

    public void renderRising(GuiGraphics graphics, int x, int y, int level) {
        int lit = Mth.clamp(level, 0, height);
        if (lit == 0) {
            return;
        }
        int dark = height - lit;
        graphics.blit(sheet, x, y + dark, u, v + dark, width, lit, sheetSize, sheetSize);
    }
}
