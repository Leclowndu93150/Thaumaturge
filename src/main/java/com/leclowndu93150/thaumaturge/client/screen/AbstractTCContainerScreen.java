package com.leclowndu93150.thaumaturge.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AbstractTCContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private final Identifier background;
    private final int backgroundWidth;
    private final int backgroundHeight;
    private final int backgroundTextureWidth;
    private final int backgroundTextureHeight;

    protected AbstractTCContainerScreen(T menu, Inventory inventory, Component title, Identifier background, int imageWidth, int imageHeight) {
        this(menu, inventory, title, background, imageWidth, imageHeight, 256, 256);
    }

    protected AbstractTCContainerScreen(T menu, Inventory inventory, Component title, Identifier background, int imageWidth, int imageHeight, int textureWidth, int textureHeight) {
        super(menu, inventory, title, imageWidth, imageHeight);
        this.background = background;
        this.backgroundWidth = imageWidth;
        this.backgroundHeight = imageHeight;
        this.backgroundTextureWidth = textureWidth;
        this.backgroundTextureHeight = textureHeight;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        extractBackgroundTexture(graphics);
        extractBackgroundOverlay(graphics, mouseX, mouseY, partialTick);
    }

    protected void extractBackgroundTexture(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos, topPos, 0.0F, 0.0F, backgroundWidth, backgroundHeight, backgroundTextureWidth, backgroundTextureHeight);
    }

    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {}

    protected final Identifier background() {
        return background;
    }
}
