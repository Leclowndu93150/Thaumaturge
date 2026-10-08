package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.content.equipment.hover.MenuThaumostaticHarness;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ThaumostaticHarnessScreen extends AbstractTTContainerScreen<MenuThaumostaticHarness> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int BLOCKED_X = 8;
    private static final int BLOCKED_Y = 142;
    private static final int BLOCKED_U = 240;
    private static final int BLOCKED_V = 0;
    private static final int BLOCKED_SIZE = 16;
    private static final int SLOT_SIZE = 18;

    public ThaumostaticHarnessScreen(MenuThaumostaticHarness menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TTScreenTextures.THAUMOSTATIC_HARNESS, WIDTH, HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (menu.blockedHotbarSlot >= 0) {
            graphics.blit(
                    background(),
                    leftPos + BLOCKED_X + menu.blockedHotbarSlot * SLOT_SIZE,
                    topPos + BLOCKED_Y,
                    BLOCKED_U,
                    BLOCKED_V,
                    BLOCKED_SIZE,
                    BLOCKED_SIZE,
                    TTScreenTextures.TEX_SIZE,
                    TTScreenTextures.TEX_SIZE);
        }
    }
}
