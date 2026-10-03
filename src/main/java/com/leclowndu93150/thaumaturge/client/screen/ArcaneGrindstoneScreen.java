package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.content.infusion.grindstone.MenuArcaneGrindstone;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ArcaneGrindstoneScreen extends AbstractContainerScreen<MenuArcaneGrindstone> {
    private static final int TEXTURE_SIZE = 256;
    private static final int ERROR_X = 92;
    private static final int ERROR_Y = 31;
    private static final int ERROR_W = 28;
    private static final int ERROR_H = 21;

    public ArcaneGrindstoneScreen(MenuArcaneGrindstone menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GRINDSTONE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
        boolean hasInput = menu.getSlot(MenuArcaneGrindstone.INPUT_SLOT).hasItem() || menu.getSlot(MenuArcaneGrindstone.ADDITIONAL_SLOT).hasItem();
        if (hasInput && !menu.getSlot(MenuArcaneGrindstone.RESULT_SLOT).hasItem()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GRINDSTONE_ERROR, leftPos + ERROR_X, topPos + ERROR_Y, ERROR_W, ERROR_H);
        }
    }
}
