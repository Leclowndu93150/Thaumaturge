package com.leclowndu93150.thaumaturge.client.screen.widget;

import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TCToggleButton extends TCButton {
    private final Identifier texture;
    private final int u;
    private final int v;
    private final int spriteWidth;
    private final int spriteHeight;
    private final int toggledU;
    private final int toggledV;
    private final int textureWidth;
    private final int textureHeight;
    private final BooleanSupplier state;

    public TCToggleButton(int x, int y, int width, int height, Identifier texture, int u, int v, int toggledU, int toggledV, int spriteWidth, int spriteHeight, int textureWidth, int textureHeight, BooleanSupplier state, Component message, Runnable onPress) {
        super(x, y, width, height, message, onPress);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.toggledU = toggledU;
        this.toggledV = toggledV;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.state = state;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int color = activeTintColor(tintColor(), isHovered(), active);
        int drawX = getX() + (getWidth() - spriteWidth) / 2;
        int drawY = getY() + (getHeight() - spriteHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, drawX, drawY, (float) u, (float) v, spriteWidth, spriteHeight, spriteWidth, spriteHeight, textureWidth, textureHeight, color);
        if (state.getAsBoolean()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, drawX, drawY, (float) toggledU, (float) toggledV, spriteWidth, spriteHeight, spriteWidth, spriteHeight, textureWidth, textureHeight,
                    color);
        }
    }
}
