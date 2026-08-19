package com.leclowndu93150.thaumaturge.client.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TCImageButton extends TCButton {
    private final Identifier texture;
    private final int u;
    private final int v;
    private final int spriteWidth;
    private final int spriteHeight;
    private final int textureWidth;
    private final int textureHeight;

    public TCImageButton(int x, int y, int width, int height, Identifier texture, int u, int v, int spriteWidth, int spriteHeight, int textureWidth, int textureHeight, Component message, Runnable onPress) {
        super(x, y, width, height, message, onPress);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public static TCImageButton centered(int centerX, int centerY, int width, int height, Identifier texture, int u, int v, int spriteWidth, int spriteHeight, int textureWidth, int textureHeight, Component message, Runnable onPress) {
        return new TCImageButton(centerToTopLeftX(centerX, width), centerToTopLeftY(centerY, height), width, height, texture, u, v, spriteWidth, spriteHeight, textureWidth, textureHeight, message,
                onPress);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int color = activeTintColor(tintColor(), isHovered(), active);
        int drawX = getX() + (getWidth() - spriteWidth) / 2;
        int drawY = getY() + (getHeight() - spriteHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, drawX, drawY, (float) u, (float) v, spriteWidth, spriteHeight, spriteWidth, spriteHeight, textureWidth, textureHeight, color);
    }
}
