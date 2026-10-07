package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class FocalDraw {
    private FocalDraw() {}

    public static void sprite(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    public static void sprite(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y, int width, int height, int color) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, color);
    }

    public static void field(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        sprite(graphics, FocalSprites.FIELD, x, y, width, height);
    }

    public static void button(GuiGraphicsExtractor graphics, int x, int y, int width, int height, Identifier glyph, int glyphSize, boolean hovered, boolean pressed) {
        button(graphics, x, y, width, height, glyph, glyphSize, hovered, pressed, true);
    }

    public static void button(GuiGraphicsExtractor graphics, int x, int y, int width, int height, Identifier glyph, int glyphSize, boolean hovered, boolean pressed, boolean enabled) {
        Identifier face = pressed ? FocalSprites.BUTTON_PRESSED : hovered && enabled ? FocalSprites.BUTTON_HOVER : FocalSprites.BUTTON_NORMAL;
        sprite(graphics, face, x, y, width, height, enabled ? FocalColors.WHITE : FocalColors.DISABLED_FACE);
        int offset = pressed ? 1 : 0;
        sprite(graphics, glyph, x + (width - glyphSize) / 2, y + (height - glyphSize) / 2 + offset, glyphSize, glyphSize, enabled ? FocalColors.WHITE : FocalColors.DISABLED_GLYPH);
    }

    public static void text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
        graphics.text(font, text, x, y, color, true);
    }

    public static void flatText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
        graphics.text(font, text, x, y, color, false);
    }

    public static void textRight(GuiGraphicsExtractor graphics, Font font, Component text, int right, int y, int color) {
        graphics.text(font, text, right - font.width(text), y, color, true);
    }

    public static boolean over(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
