package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.content.device.sprayer.BlockEntityPotionSprayer;
import com.leclowndu93150.thaumaturge.content.device.sprayer.MenuPotionSprayer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class PotionSprayerScreen extends AbstractTCContainerScreen<MenuPotionSprayer> {
    private static final Identifier TEXTURE = TCIds.rl("textures/gui/gui_potion_sprayer.png");
    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 233;

    private static final int CHARGE_BAR_X = 128;
    private static final int CHARGE_BAR_Y = 36;
    private static final int CHARGE_BAR_U = 232;
    private static final int CHARGE_BAR_WIDTH = 8;
    private static final int CHARGE_SEGMENT_HEIGHT = 9;
    private static final int CHARGE_SCROLL_PERIOD = 256;
    private static final int CHARGE_OVERLAY_X = 125;
    private static final int CHARGE_OVERLAY_Y = 28;
    private static final int CHARGE_OVERLAY_U = 205;
    private static final int CHARGE_OVERLAY_V = 28;
    private static final int CHARGE_OVERLAY_WIDTH = 14;
    private static final int CHARGE_OVERLAY_HEIGHT = 88;

    private static final int TAG_BASE_X = 79;
    private static final int TAG_BASE_Y = 31;
    private static final int TAG_COLUMN_STRIDE = 22;
    private static final int TAG_ROW_STRIDE = 16;
    private static final int BAR_BASE_X = 96;
    private static final int BAR_BASE_Y = 46;
    private static final int BAR_U = 192;
    private static final int BAR_V = 56;
    private static final int BAR_WIDTH = 2;
    private static final int BAR_HEIGHT = 14;
    private static final int BAR_BACKGROUND_TINT = 0xFF333333;

    private static final int TAG_TEXTURE_SIZE = 32;
    private static final int TAG_DRAW_SIZE = 16;

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    public PotionSprayerScreen(MenuPotionSprayer menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    private BlockEntityPotionSprayer sprayer() {
        if (minecraft != null && minecraft.level != null && minecraft.level.getBlockEntity(menu.sprayerPos()) instanceof BlockEntityPotionSprayer be) {
            return be;
        }
        return null;
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BlockEntityPotionSprayer sprayer = sprayer();
        if (sprayer == null || minecraft == null || minecraft.player == null) {
            return;
        }
        int charges = sprayer.charges();
        if (charges > 0) {
            int scroll = minecraft.player.tickCount % CHARGE_SCROLL_PERIOD;
            int tint = 0xFF000000 | (sprayer.color() & 0x00FFFFFF);
            int barHeight = charges * CHARGE_SEGMENT_HEIGHT;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + CHARGE_BAR_X, topPos + CHARGE_BAR_Y + (BlockEntityPotionSprayer.MAX_CHARGES - charges) * CHARGE_SEGMENT_HEIGHT,
                    (float) CHARGE_BAR_U, (float) scroll, CHARGE_BAR_WIDTH, barHeight, CHARGE_BAR_WIDTH, barHeight, 256, 256, tint);
        }
        drawAspects(graphics, sprayer);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + CHARGE_OVERLAY_X, topPos + CHARGE_OVERLAY_Y, CHARGE_OVERLAY_U, CHARGE_OVERLAY_V, CHARGE_OVERLAY_WIDTH, CHARGE_OVERLAY_HEIGHT,
                256, 256);
    }

    private void drawAspects(GuiGraphicsExtractor graphics, BlockEntityPotionSprayer sprayer) {
        AspectList recipe = sprayer.recipe();
        AspectList progress = sprayer.progress();
        int pos = 0;
        for (AspectInstance entry : recipe.entries()) {
            int barX = leftPos + BAR_BASE_X + TAG_COLUMN_STRIDE * (pos % 2);
            int barBottom = topPos + BAR_BASE_Y + TAG_ROW_STRIDE * (pos / 2);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, barX, barBottom - BAR_HEIGHT, (float) BAR_U, (float) BAR_V, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT, 256, 256,
                    BAR_BACKGROUND_TINT);
            int filled = (int) ((float) progress.amountOf(entry.aspect()) / entry.amount() * BAR_HEIGHT);
            if (filled > 0) {
                int tint = 0xFF000000 | (entry.aspect().value().color() & 0x00FFFFFF);
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, barX, barBottom - filled, (float) BAR_U, (float) BAR_V, BAR_WIDTH, filled, BAR_WIDTH, filled, 256, 256, tint);
            }
            pos++;
        }
        pos = 0;
        for (AspectInstance entry : recipe.entries()) {
            AspectTagRenderer.render(graphics, font, leftPos + TAG_BASE_X + TAG_COLUMN_STRIDE * (pos % 2), topPos + TAG_BASE_Y + TAG_ROW_STRIDE * (pos / 2), entry.aspect(), entry.amount());
            pos++;
        }
    }
}
