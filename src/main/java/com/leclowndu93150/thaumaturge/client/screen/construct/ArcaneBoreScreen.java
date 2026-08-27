package com.leclowndu93150.thaumaturge.client.screen.construct;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreHost;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreTool;
import com.leclowndu93150.thaumaturge.content.device.bore.MenuArcaneBore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public final class ArcaneBoreScreen extends AbstractTCContainerScreen<MenuArcaneBore> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/gui/gui_arcanebore.png");
    private static final int IMAGE_WIDTH = 175;
    private static final int IMAGE_HEIGHT = 232;
    private static final int HEALTH_BAR_X = 68;
    private static final int HEALTH_BAR_Y = 59;
    private static final int HEALTH_BAR_U = 192;
    private static final int HEALTH_BAR_V = 48;
    private static final int HEALTH_BAR_WIDTH = 39;
    private static final int HEALTH_BAR_HEIGHT = 6;
    private static final int BROKEN_X = 80;
    private static final int BROKEN_Y = 29;
    private static final int BROKEN_U = 240;
    private static final int BROKEN_V = 0;
    private static final int BROKEN_SIZE = 16;
    private static final int STATS_X = 124;
    private static final int STATS_Y = 18;
    private static final float STATS_SCALE = 0.5F;
    private static final int STATS_COLUMN_2 = 64;
    private static final int STATS_LINE_2 = 10;
    private static final int PROPS_HEADER_Y = 24;
    private static final int PROPS_FIRST_Y = 34;
    private static final int PROPS_INDENT = 4;
    private static final int PROPS_LINE_STEP = 9;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_REFINING = 0xFFC0C0C0;
    private static final int COLOR_FORTUNE = 0xFFEEC64A;
    private static final int COLOR_SILK = 0xFF8080FF;

    public ArcaneBoreScreen(MenuArcaneBore menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int xm, int ym) {}

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ArcaneBoreHost bore = menu.bore();
        if (bore == null) {
            return;
        }
        int fill = (int) (HEALTH_BAR_WIDTH * (bore.boreHealth() / bore.boreMaxHealth()));
        graphics.blit(
                TEXTURE,
                leftPos + HEALTH_BAR_X,
                topPos + HEALTH_BAR_Y,
                HEALTH_BAR_U,
                HEALTH_BAR_V,
                fill,
                HEALTH_BAR_HEIGHT,
                256,
                256);
        ItemStack held = menu.getSlot(0).getItem();
        if (!held.isEmpty() && held.isDamageableItem() && held.getDamageValue() + 1 >= held.getMaxDamage()) {
            graphics.blit(
                    TEXTURE,
                    leftPos + BROKEN_X,
                    topPos + BROKEN_Y,
                    BROKEN_U,
                    BROKEN_V,
                    BROKEN_SIZE,
                    BROKEN_SIZE,
                    256,
                    256);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + STATS_X, topPos + STATS_Y, 0.0F);
        graphics.pose().scale(STATS_SCALE, STATS_SCALE, 1.0F);
        graphics.drawString(
                font,
                Component.translatable("gui.thaumaturge.bore.width", 1 + ArcaneBoreTool.digRadius(held) * 2),
                0,
                0,
                COLOR_WHITE,
                true);
        graphics.drawString(
                font,
                Component.translatable("gui.thaumaturge.bore.depth", ArcaneBoreTool.digDepth(held)),
                STATS_COLUMN_2,
                0,
                COLOR_WHITE,
                true);
        graphics.drawString(
                font,
                Component.translatable(
                        "gui.thaumaturge.bore.speed",
                        ArcaneBoreTool.digSpeed(bore.boreLevel(), held, Blocks.STONE.defaultBlockState())),
                0,
                STATS_LINE_2,
                COLOR_WHITE,
                true);
        int refining = ArcaneBoreTool.refining(held);
        int fortune = ArcaneBoreTool.fortune(bore.boreLevel(), held);
        boolean silk = ArcaneBoreTool.silkTouch(bore.boreLevel(), held);
        if (silk || refining > 0 || fortune > 0) {
            graphics.drawString(
                    font,
                    Component.translatable("gui.thaumaturge.bore.properties"),
                    0,
                    PROPS_HEADER_Y,
                    COLOR_WHITE,
                    true);
        }
        int lineY = PROPS_FIRST_Y;
        if (refining > 0) {
            graphics.drawString(
                    font,
                    Component.translatable("gui.thaumaturge.bore.refining", refining),
                    PROPS_INDENT,
                    lineY,
                    COLOR_REFINING,
                    true);
            lineY += PROPS_LINE_STEP;
        }
        if (fortune > 0) {
            graphics.drawString(
                    font,
                    Component.translatable("gui.thaumaturge.bore.fortune", fortune),
                    PROPS_INDENT,
                    lineY,
                    COLOR_FORTUNE,
                    true);
            lineY += PROPS_LINE_STEP;
        }
        if (silk) {
            graphics.drawString(
                    font,
                    Component.translatable("gui.thaumaturge.bore.silktouch"),
                    PROPS_INDENT,
                    lineY,
                    COLOR_SILK,
                    true);
        }
        graphics.pose().popPose();
    }
}
