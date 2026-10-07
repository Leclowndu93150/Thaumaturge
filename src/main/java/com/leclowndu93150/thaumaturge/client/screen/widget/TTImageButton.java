package com.leclowndu93150.thaumaturge.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TTImageButton extends TTButton {
    private final ResourceLocation texture;
    private final int u;
    private final int v;
    private final int spriteWidth;
    private final int spriteHeight;
    private final int textureWidth;
    private final int textureHeight;

    public TTImageButton(
            int x,
            int y,
            int width,
            int height,
            ResourceLocation texture,
            int u,
            int v,
            int spriteWidth,
            int spriteHeight,
            int textureWidth,
            int textureHeight,
            Component message,
            Runnable onPress) {
        super(x, y, width, height, message, onPress);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public static TTImageButton centered(
            int centerX,
            int centerY,
            int width,
            int height,
            ResourceLocation texture,
            int u,
            int v,
            int spriteWidth,
            int spriteHeight,
            int textureWidth,
            int textureHeight,
            Component message,
            Runnable onPress) {
        return new TTImageButton(
                centerToTopLeftX(centerX, width),
                centerToTopLeftY(centerY, height),
                width,
                height,
                texture,
                u,
                v,
                spriteWidth,
                spriteHeight,
                textureWidth,
                textureHeight,
                message,
                onPress);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int color = activeTintColor(tintColor(), isHovered(), active);
        int drawX = getX() + (getWidth() - spriteWidth) / 2;
        int drawY = getY() + (getHeight() - spriteHeight) / 2;
        WidgetRender.blitTinted(
                graphics,
                texture,
                drawX,
                drawY,
                (float) u,
                (float) v,
                spriteWidth,
                spriteHeight,
                textureWidth,
                textureHeight,
                color);
    }
}
