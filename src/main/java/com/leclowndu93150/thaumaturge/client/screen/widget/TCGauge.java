package com.leclowndu93150.thaumaturge.client.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public record TCGauge(Identifier sheet, int sheetSize, int u, int v, int width, int height) {
    public void extractFull(GuiGraphicsExtractor graphics, int x, int y) {
        extractRising(graphics, x, y, height);
    }

    public void extractRising(GuiGraphicsExtractor graphics, int x, int y, int level) {
        int lit = Mth.clamp(level, 0, height);
        if (lit == 0) {
            return;
        }
        int dark = height - lit;
        graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x, y + dark, u, v + dark, width, lit, sheetSize, sheetSize);
    }
}
