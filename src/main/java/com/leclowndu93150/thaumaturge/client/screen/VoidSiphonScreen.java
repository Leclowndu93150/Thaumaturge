package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVoidSiphon;
import com.leclowndu93150.thaumaturge.content.device.MenuVoidSiphon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.player.Inventory;

public final class VoidSiphonScreen extends AbstractTTContainerScreen<MenuVoidSiphon> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/gui/gui_void_siphon.png");
    private static final int PORTAL_X = 62;
    private static final int PORTAL_Y = 14;
    private static final int PORTAL_SIZE = 52;
    private static final int PROGRESS_COLOR = ARGB32.color(160, 60, 0, 90);

    public VoidSiphonScreen(MenuVoidSiphon menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, 176, 166);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float ratio = Math.min(1.0F, menu.progress() / (float) BlockEntityVoidSiphon.PROGRESS_REQUIRED);
        int barHeight = (int) (PORTAL_SIZE * ratio);
        if (barHeight > 0) {
            graphics.fill(
                    leftPos + PORTAL_X,
                    topPos + PORTAL_Y + PORTAL_SIZE - barHeight,
                    leftPos + PORTAL_X + PORTAL_SIZE,
                    topPos + PORTAL_Y + PORTAL_SIZE,
                    PROGRESS_COLOR);
        }
    }
}
