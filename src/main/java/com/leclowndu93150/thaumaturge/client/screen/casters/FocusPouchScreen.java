package com.leclowndu93150.thaumaturge.client.screen.casters;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.content.casters.MenuFocusPouch;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class FocusPouchScreen extends AbstractTCContainerScreen<MenuFocusPouch> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_focuspouch.png");
    private static final int WIDTH = 175;
    private static final int HEIGHT = 232;
    private static final int BLOCKED_X = 8;
    private static final int BLOCKED_Y = 209;
    private static final int BLOCKED_U = 240;
    private static final int BLOCKED_V = 0;
    private static final int BLOCKED_SIZE = 16;
    private static final int SLOT_SIZE = 18;

    public FocusPouchScreen(MenuFocusPouch menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, WIDTH, HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (menu.blockedHotbarSlot >= 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + BLOCKED_X + menu.blockedHotbarSlot * SLOT_SIZE, topPos + BLOCKED_Y, BLOCKED_U, BLOCKED_V, BLOCKED_SIZE, BLOCKED_SIZE, 256,
                    256);
        }
    }
}
