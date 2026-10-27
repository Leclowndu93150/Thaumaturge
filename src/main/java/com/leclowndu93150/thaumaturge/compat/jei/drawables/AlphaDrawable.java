package com.leclowndu93150.thaumaturge.compat.jei.drawables;

import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class AlphaDrawable implements IDrawableStatic {
    private final ResourceLocation identifier;
    private final int u;
    private final int v;
    private final int width;
    private final int height;
    private final int paddingTop;
    private final int paddingBottom;
    private final int paddingLeft;
    private final int paddingRight;

    public AlphaDrawable(ResourceLocation identifier, int u, int v, int width, int height) {
        this(identifier, u, v, width, height, 0, 0, 0, 0);
    }

    public AlphaDrawable(
            ResourceLocation identifier,
            int u,
            int v,
            int width,
            int height,
            int paddingTop,
            int paddingBottom,
            int paddingLeft,
            int paddingRight) {
        this.identifier = identifier;

        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;

        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
    }

    @Override
    public int getWidth() {
        return width + paddingLeft + paddingRight;
    }

    @Override
    public int getHeight() {
        return height + paddingTop + paddingBottom;
    }

    @Override
    public void draw(GuiGraphics guiGraphics) {
        draw(guiGraphics, 0, 0);
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
    }

    @Override
    public void draw(
            GuiGraphics guiGraphics,
            int xOffset,
            int yOffset,
            int maskTop,
            int maskBottom,
            int maskLeft,
            int maskRight) {
        int x = xOffset + this.paddingLeft + maskLeft;
        int y = yOffset + this.paddingTop + maskTop;
        int u = this.u + maskLeft;
        int v = this.v + maskTop;
        int width = this.width - maskRight - maskLeft;
        int height = this.height - maskBottom - maskTop;
        GuiBlend.withAlphaBlend(guiGraphics, () -> guiGraphics.blit(identifier, x, y, u, v, width, height, 512, 512));
    }
}
