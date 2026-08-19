package com.leclowndu93150.thaumaturge.compat.jei.drawables;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class AlphaDrawable implements IDrawableStatic {
    private final Identifier identifier;
    private final int u;
    private final int v;
    private final int width;
    private final int height;
    private final int paddingTop;
    private final int paddingBottom;
    private final int paddingLeft;
    private final int paddingRight;

    public AlphaDrawable(Identifier identifier, int u, int v, int width, int height) {
        this(identifier, u, v, width, height, 0, 0, 0, 0);
    }

    public AlphaDrawable(Identifier identifier, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
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
    public void draw(GuiGraphicsExtractor guiGraphics) {
        draw(guiGraphics, 0, 0);
    }

    @Override
    public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
        draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
    }

    @Override
    public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
        int x = xOffset + this.paddingLeft + maskLeft;
        int y = yOffset + this.paddingTop + maskTop;
        int u = this.u + maskLeft;
        int v = this.v + maskTop;
        int width = this.width - maskRight - maskLeft;
        int height = this.height - maskBottom - maskTop;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, identifier, x, y, u, v, width, height, 512, 512);
    }
}
