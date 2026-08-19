package com.leclowndu93150.thaumaturge.client.render.aspect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class AspectTagRenderer {
    public static final int TAG_SIZE = 16;
    public static final int TEXTURE_SIZE = 32;

    private static final Identifier UNKNOWN_TEXTURE = TCIds.rl("textures/aspects/_unknown.png");
    private static final float UNKNOWN_ALPHA = 0.45F;
    private static final float DEDUCIBLE_ALPHA = 1.0F;

    public enum BlendMode {
        ALPHA, ADDITIVE
    }

    private static final int AMOUNT_COLOR = 0xFFFFFFFF;
    private static final int AMOUNT_OUTLINE_COLOR = 0xFF000000;
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#######.##");

    private static final int BONUS_OFFSET = -4;
    private static final int BONUS_BADGE_V = 80;
    private static final int BONUS_BADGE_SIZE = 16;
    private static final int BONUS_BADGE_STRIDE = 16;
    private static final int BONUS_BADGE_CYCLE = 16;
    private static final int BONUS_BADGE_TEXTURE_SIZE = 256;

    private AspectTagRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect) {
        render(graphics, Minecraft.getInstance().font, (double) x, (double) y, aspect, 0.0F, 0, 0.0, BlendMode.ALPHA, 1.0F, false);
    }

    public static void renderUnknown(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect) {
        render(graphics, Minecraft.getInstance().font, (double) x, (double) y, aspect, 0.0F, 0, 0.0, BlendMode.ALPHA, 1.0F, true);
    }

    public static void renderMaskedChip(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect, AspectKnowledge knowledge) {
        renderMaskedChip(graphics, x, y, aspect, knowledge == AspectKnowledge.DEDUCIBLE ? DEDUCIBLE_ALPHA : UNKNOWN_ALPHA);
    }

    public static void renderUnknownChip(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect) {
        renderMaskedChip(graphics, x, y, aspect, UNKNOWN_ALPHA);
    }

    private static void renderMaskedChip(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect, float alpha) {
        int tint = ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F);
        int color = aspect != null && aspect.value() != null ? (tint & 0xFF000000) | (aspect.value().color() & 0x00FFFFFF) : tint;
        graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_TEXTURE, x, y, 0.0F, 0.0F, TAG_SIZE, TAG_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, color);
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, int x, int y, Holder<IAspect> aspect, float amount) {
        render(graphics, font, (double) x, (double) y, aspect, amount, 0, 0.0, BlendMode.ALPHA, 1.0F, false);
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, int x, int y, Holder<IAspect> aspect, float amount, int bonus, float alpha, boolean bw) {
        render(graphics, font, (double) x, (double) y, aspect, amount, bonus, 0.0, BlendMode.ALPHA, alpha, bw);
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect, float amount, int bonus, float alpha, boolean bw) {
        render(graphics, Minecraft.getInstance().font, (double) x, (double) y, aspect, amount, bonus, 0.0, BlendMode.ALPHA, alpha, bw);
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect, float amount, int bonus, double z) {
        render(graphics, Minecraft.getInstance().font, (double) x, (double) y, aspect, amount, bonus, z, BlendMode.ALPHA, 1.0F, false);
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect, float amount, int bonus, double z, BlendMode blend, float alpha) {
        render(graphics, Minecraft.getInstance().font, (double) x, (double) y, aspect, amount, bonus, z, blend, alpha, false);
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, double x, double y, Holder<IAspect> aspect, float amount, int bonus, double z, BlendMode blend, float alpha, boolean bw) {
        if (aspect == null || aspect.value() == null)
            return;
        drawIcon(graphics, x, y, z, aspect, blend, alpha, bw);
        if (amount > 0.0F) {
            drawAmount(graphics, font, x, y, amount);
        }
        if (bonus > 0) {
            drawBonus(graphics, font, x, y, z, bonus);
        }
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, double x, double y, double z, Holder<IAspect> aspect, BlendMode blend, float alpha, boolean bw) {
        IAspect value = aspect.value();
        int color = colorOf(value, alpha, bw);
        RenderPipeline pipeline = blend == BlendMode.ADDITIVE ? TCRenderPipelines.GUI_TEXTURED_ADDITIVE : RenderPipelines.GUI_TEXTURED;
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) x, (float) y);
        graphics.blit(pipeline, value.texture(), 0, 0, 0.0F, 0.0F, TAG_SIZE, TAG_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, color);
        graphics.pose().popMatrix();
    }

    private static void drawAmount(GuiGraphicsExtractor graphics, Font font, double x, double y, float amount) {
        String text = AMOUNT_FORMAT.format(amount);
        int width = font.width(text);
        int fontHeight = font.lineHeight;
        boolean large = ThaumaturgeClientConfig.largeTagText();
        float textScale = large ? 1.0F : 0.5F;
        float q = large ? 0.5F : 1.0F;
        float textX = (32.0F - (float) width + (float) ((int) x) * 2.0F) * q;
        float textY = (32.0F - (float) fontHeight + (float) ((int) y) * 2.0F) * q;
        graphics.pose().pushMatrix();
        graphics.pose().scale(textScale, textScale);
        graphics.text(font, Component.literal(text), (int) (textX - 1.0F), (int) textY, AMOUNT_OUTLINE_COLOR, false);
        graphics.text(font, Component.literal(text), (int) (textX + 1.0F), (int) textY, AMOUNT_OUTLINE_COLOR, false);
        graphics.text(font, Component.literal(text), (int) textX, (int) (textY - 1.0F), AMOUNT_OUTLINE_COLOR, false);
        graphics.text(font, Component.literal(text), (int) textX, (int) (textY + 1.0F), AMOUNT_OUTLINE_COLOR, false);
        graphics.text(font, Component.literal(text), (int) textX, (int) textY, AMOUNT_COLOR, false);
        graphics.pose().popMatrix();
    }

    private static void drawBonus(GuiGraphicsExtractor graphics, Font font, double x, double y, double z, int bonus) {
        Minecraft mc = Minecraft.getInstance();
        int ticks = mc.player != null ? mc.player.tickCount : mc.gui.getGuiTicks();
        int frame = ticks % BONUS_BADGE_CYCLE;
        int u = frame * BONUS_BADGE_STRIDE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, ParticleTextures.PARTICLES, (int) x + BONUS_OFFSET, (int) y + BONUS_OFFSET, (float) u, (float) BONUS_BADGE_V, BONUS_BADGE_SIZE, BONUS_BADGE_SIZE,
                BONUS_BADGE_SIZE, BONUS_BADGE_SIZE, BONUS_BADGE_TEXTURE_SIZE, BONUS_BADGE_TEXTURE_SIZE, 0xFFFFFFFF);
        if (bonus > 1) {
            String text = Integer.toString(bonus);
            int half = font.width(text) / 2;
            int fontHeight = font.lineHeight;
            boolean large = ThaumaturgeClientConfig.largeTagText();
            float textScale = large ? 1.0F : 0.5F;
            float q = large ? 0.5F : 1.0F;
            float textX = (8.0F - (float) half + (float) ((int) x) * 2.0F) * q;
            float textY = (15.0F - (float) fontHeight + (float) ((int) y) * 2.0F) * q;
            graphics.pose().pushMatrix();
            graphics.pose().scale(textScale, textScale);
            graphics.text(font, Component.literal(text), (int) textX, (int) textY, AMOUNT_COLOR, true);
            graphics.pose().popMatrix();
        }
    }

    public static int colorOf(IAspect aspect, float alpha, boolean bw) {
        if (bw) {
            int a = Math.max(0, Math.min(255, (int) (alpha * 0.8F * 255.0F)));
            return (a << 24) | 0x1A1A1A;
        }
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        return (a << 24) | (aspect.color() & 0x00FFFFFF);
    }
}
