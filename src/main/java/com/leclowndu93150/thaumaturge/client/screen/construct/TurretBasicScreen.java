package com.leclowndu93150.thaumaturge.client.screen.construct;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbow;
import com.leclowndu93150.thaumaturge.content.entity.construct.MenuTurretBasic;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class TurretBasicScreen<T extends MenuTurretBasic> extends AbstractTCContainerScreen<T> {
    private static final Identifier TEXTURE = TCIds.rl("textures/gui/gui_turret_basic.png");
    static final int IMAGE_WIDTH = 175;
    static final int IMAGE_HEIGHT = 232;
    static final int HEALTH_BAR_Y = 59;
    static final int HEALTH_BAR_U = 192;
    static final int HEALTH_BAR_V = 48;
    static final int HEALTH_BAR_WIDTH = 39;
    static final int HEALTH_BAR_HEIGHT = 6;
    private static final int HEALTH_BAR_X = 68;

    public TurretBasicScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    protected TurretBasicScreen(T menu, Inventory inventory, Component title, Identifier texture) {
        super(menu, inventory, title, texture, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        drawHealthBar(graphics, HEALTH_BAR_X, TEXTURE);
    }

    void drawHealthBar(GuiGraphicsExtractor graphics, int barX, Identifier texture) {
        EntityTurretCrossbow turret = menu.turret();
        if (turret == null) {
            return;
        }
        int fill = (int) (HEALTH_BAR_WIDTH * (turret.getHealth() / turret.getMaxHealth()));
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + barX, topPos + HEALTH_BAR_Y, HEALTH_BAR_U, HEALTH_BAR_V, fill, HEALTH_BAR_HEIGHT, 256, 256);
    }
}
