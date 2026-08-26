package com.leclowndu93150.thaumaturge.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class TCLabelButton extends TCImageButton {
    private static final float LABEL_SCALE = 0.5F;
    private static final int LABEL_BASELINE = -4;
    private static final int LABEL_IDLE = 0xFFFFFFFF;
    private static final int LABEL_HOVERED = 0xFFFFFFA0;
    private static final int LABEL_DISABLED = 0xFFA0A0A0;

    private TCLabelButton(
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
        super(
                x,
                y,
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

    public static TCLabelButton centered(
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
        return new TCLabelButton(
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
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        Component message = getMessage();
        if (message == null || message.getString().isEmpty()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int color = !active ? LABEL_DISABLED : isHovered() ? LABEL_HOVERED : LABEL_IDLE;
        graphics.pose().pushPose();
        graphics.pose().translate(getX() + getWidth() / 2.0F, getY() + getHeight() / 2.0F, 0.0F);
        graphics.pose().scale(LABEL_SCALE, LABEL_SCALE, 1.0F);
        graphics.drawString(font, message, -font.width(message) / 2, LABEL_BASELINE, color, true);
        graphics.pose().popPose();
    }
}
