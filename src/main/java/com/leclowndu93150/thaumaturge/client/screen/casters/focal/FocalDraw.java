package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;

public final class FocalDraw {
    private FocalDraw() {}

    public static void sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        graphics.blitSprite(sprite, x, y, width, height);
    }

    public static void sprite(
            GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int color) {
        graphics.setColor(
                ARGB32.red(color) / 255.0F,
                ARGB32.green(color) / 255.0F,
                ARGB32.blue(color) / 255.0F,
                ARGB32.alpha(color) / 255.0F);
        graphics.blitSprite(sprite, x, y, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void field(GuiGraphics graphics, int x, int y, int width, int height) {
        sprite(graphics, FocalSprites.FIELD, x, y, width, height);
    }

    public static void button(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            ResourceLocation glyph,
            int glyphSize,
            boolean hovered,
            boolean pressed) {
        button(graphics, x, y, width, height, glyph, glyphSize, hovered, pressed, true);
    }

    public static void button(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            ResourceLocation glyph,
            int glyphSize,
            boolean hovered,
            boolean pressed,
            boolean enabled) {
        ResourceLocation face = pressed
                ? FocalSprites.BUTTON_PRESSED
                : hovered && enabled ? FocalSprites.BUTTON_HOVER : FocalSprites.BUTTON_NORMAL;
        sprite(graphics, face, x, y, width, height, enabled ? FocalColors.WHITE : FocalColors.DISABLED_FACE);
        int offset = pressed ? 1 : 0;
        sprite(
                graphics,
                glyph,
                x + (width - glyphSize) / 2,
                y + (height - glyphSize) / 2 + offset,
                glyphSize,
                glyphSize,
                enabled ? FocalColors.WHITE : FocalColors.DISABLED_GLYPH);
    }

    public static void text(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    public static void flatText(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, false);
    }

    public static void textRight(GuiGraphics graphics, Font font, Component text, int right, int y, int color) {
        graphics.drawString(font, text, right - font.width(text), y, color, true);
    }

    public static boolean over(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
