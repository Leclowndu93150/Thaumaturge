package com.leclowndu93150.thaumaturge.client.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class TTHoverButton extends TTButton {
    private final TTButtonIcon icon;
    private final int iconSize;

    public TTHoverButton(int x, int y, int width, int height, TTButtonIcon icon, Component message, Runnable onPress) {
        super(x, y, width, height, message, onPress);
        this.icon = icon;
        this.iconSize = Math.min(width, height);
    }

    public static TTHoverButton centered(int centerX, int centerY, int size, TTButtonIcon icon, Component message, Runnable onPress) {
        return new TTHoverButton(centerToTopLeftX(centerX, size), centerToTopLeftY(centerY, size), size, size, icon, message, onPress);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int tint = activeTintColor(tintColor(), isHovered(), active);
        int drawX = getX() + (getWidth() - iconSize) / 2;
        int drawY = getY() + (getHeight() - iconSize) / 2;
        icon.draw(graphics, drawX, drawY, iconSize, tint);
    }
}
