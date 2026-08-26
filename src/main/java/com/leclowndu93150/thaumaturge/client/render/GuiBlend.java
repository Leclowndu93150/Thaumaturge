package com.leclowndu93150.thaumaturge.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

public final class GuiBlend {
    private GuiBlend() {}

    public static LayeredDraw.Layer alphaBlendedLayer(LayeredDraw.Layer layer) {
        return (graphics, deltaTracker) -> withAlphaBlend(graphics, () -> layer.render(graphics, deltaTracker));
    }

    public static void withAlphaBlend(GuiGraphics graphics, Runnable draw) {
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            draw.run();
        } finally {
            graphics.flush();
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    public static void blitTinted(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int textureWidth,
            int textureHeight,
            int argb) {
        RenderSystem.enableBlend();
        setColor(graphics, argb);
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void blitTinted(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height,
            float u,
            float v,
            int regionWidth,
            int regionHeight,
            int textureWidth,
            int textureHeight,
            int argb) {
        RenderSystem.enableBlend();
        setColor(graphics, argb);
        graphics.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void blitAdditive(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int textureWidth,
            int textureHeight,
            int argb) {
        beginAdditive(graphics, argb);
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        endAdditive(graphics);
    }

    public static void blitAdditive(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height,
            float u,
            float v,
            int regionWidth,
            int regionHeight,
            int textureWidth,
            int textureHeight,
            int argb) {
        beginAdditive(graphics, argb);
        graphics.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        endAdditive(graphics);
    }

    private static void beginAdditive(GuiGraphics graphics, int argb) {
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        setColor(graphics, argb);
    }

    private static void endAdditive(GuiGraphics graphics) {
        graphics.flush();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }

    private static void setColor(GuiGraphics graphics, int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0F;
        float r = ((argb >> 16) & 0xFF) / 255.0F;
        float g = ((argb >> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        graphics.setColor(r, g, b, a);
    }
}
