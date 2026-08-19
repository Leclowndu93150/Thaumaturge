package com.leclowndu93150.thaumaturge.client.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class TCHoverButton extends TCButton {
    private final TCButtonIcon icon;
    private final int iconSize;

    public TCHoverButton(int x, int y, int width, int height, TCButtonIcon icon, Component message, Runnable onPress) {
        super(x, y, width, height, message, onPress);
        this.icon = icon;
        this.iconSize = Math.min(width, height);
    }

    public static TCHoverButton centered(int centerX, int centerY, int size, TCButtonIcon icon, Component message, Runnable onPress) {
        return new TCHoverButton(centerToTopLeftX(centerX, size), centerToTopLeftY(centerY, size), size, size, icon, message, onPress);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int tint = activeTintColor(tintColor(), isHovered(), active);
        int drawX = getX() + (getWidth() - iconSize) / 2;
        int drawY = getY() + (getHeight() - iconSize) / 2;
        icon.draw(graphics, drawX, drawY, iconSize, tint);
    }
}
