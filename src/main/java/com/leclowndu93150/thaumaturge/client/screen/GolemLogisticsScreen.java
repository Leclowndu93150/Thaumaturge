package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.content.golem.MenuGolemLogistics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class GolemLogisticsScreen extends AbstractContainerScreen<MenuGolemLogistics> {
    public GolemLogisticsScreen(MenuGolemLogistics menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 132;
        inventoryLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFCEC6C6);
        graphics.fill(leftPos + 7, topPos + 17, leftPos + 169, topPos + 127, 0xFF373737);
        for (int row = 0; row < MenuGolemLogistics.ROWS; row++) {
            for (int column = 0; column < 9; column++) {
                int x = leftPos + 8 + column * 18;
                int y = topPos + 18 + row * 18;
                graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
                graphics.fill(x, y, x + 16, y + 16, 0xFF373737);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
