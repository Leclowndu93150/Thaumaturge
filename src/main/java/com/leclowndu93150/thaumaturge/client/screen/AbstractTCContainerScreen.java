package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AbstractTCContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private final ResourceLocation background;
    private final int backgroundWidth;
    private final int backgroundHeight;
    private final int backgroundTextureWidth;
    private final int backgroundTextureHeight;

    protected AbstractTCContainerScreen(
            T menu,
            Inventory inventory,
            Component title,
            ResourceLocation background,
            int imageWidth,
            int imageHeight) {
        this(menu, inventory, title, background, imageWidth, imageHeight, 256, 256);
    }

    protected AbstractTCContainerScreen(
            T menu,
            Inventory inventory,
            Component title,
            ResourceLocation background,
            int imageWidth,
            int imageHeight,
            int textureWidth,
            int textureHeight) {
        super(menu, inventory, title);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.background = background;
        this.backgroundWidth = imageWidth;
        this.backgroundHeight = imageHeight;
        this.backgroundTextureWidth = textureWidth;
        this.backgroundTextureHeight = textureHeight;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiBlend.withAlphaBlend(graphics, () -> {
            renderBackgroundTexture(graphics);
            renderBackgroundOverlay(graphics, mouseX, mouseY, partialTick);
        });
    }

    protected void renderBackgroundTexture(GuiGraphics graphics) {
        graphics.blit(
                background,
                leftPos,
                topPos,
                0.0F,
                0.0F,
                backgroundWidth,
                backgroundHeight,
                backgroundTextureWidth,
                backgroundTextureHeight);
    }

    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    protected final ResourceLocation background() {
        return background;
    }
}
