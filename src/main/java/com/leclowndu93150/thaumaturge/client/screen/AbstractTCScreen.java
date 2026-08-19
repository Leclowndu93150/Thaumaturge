package com.leclowndu93150.thaumaturge.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class AbstractTCScreen extends Screen {
    private final @Nullable Identifier background;
    private final int backgroundTextureWidth;
    private final int backgroundTextureHeight;

    protected AbstractTCScreen(Component title) {
        this(title, null, 256, 256);
    }

    protected AbstractTCScreen(Component title, @Nullable Identifier background) {
        this(title, background, 256, 256);
    }

    protected AbstractTCScreen(Component title, @Nullable Identifier background, int backgroundTextureWidth, int backgroundTextureHeight) {
        super(title);
        this.background = background;
        this.backgroundTextureWidth = backgroundTextureWidth;
        this.backgroundTextureHeight = backgroundTextureHeight;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        if (background != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, background, 0, 0, 0.0F, 0.0F, width, height, width, height, backgroundTextureWidth, backgroundTextureHeight);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
