package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.device.mirror.MenuHandMirror;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class HandMirrorScreen extends AbstractTCContainerScreen<MenuHandMirror> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_handmirror.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int BLOCKED_X = 8;
    private static final int BLOCKED_Y = 142;
    private static final int BLOCKED_U = 240;
    private static final int BLOCKED_V = 0;
    private static final int BLOCKED_SIZE = 16;
    private static final int SLOT_SIZE = 18;

    public HandMirrorScreen(MenuHandMirror menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, WIDTH, HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + BLOCKED_X + menu.mirrorHotbarSlot * SLOT_SIZE, topPos + BLOCKED_Y, BLOCKED_U, BLOCKED_V, BLOCKED_SIZE, BLOCKED_SIZE, 256, 256);
    }
}
