package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.pech.MenuPech;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PechScreen extends AbstractTTContainerScreen<MenuPech> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/gui/gui_pech.png");
    private static final int IMAGE_WIDTH = 175;
    private static final int IMAGE_HEIGHT = 232;
    private static final int TRADE_BUTTON_X = 67;
    private static final int TRADE_BUTTON_Y = 24;
    private static final int TRADE_BUTTON_SIZE = 25;
    private static final int TRADE_BUTTON_U = 176;
    private static final int TRADE_BUTTON_V = 0;
    private static final float DICE_VOLUME = 0.5F;

    public PechScreen(MenuPech menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (menu.canTrade()) {
            graphics.blit(
                    TEXTURE,
                    leftPos + TRADE_BUTTON_X,
                    topPos + TRADE_BUTTON_Y,
                    TRADE_BUTTON_U,
                    TRADE_BUTTON_V,
                    TRADE_BUTTON_SIZE,
                    TRADE_BUTTON_SIZE,
                    256,
                    256);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX - (leftPos + TRADE_BUTTON_X);
        int my = (int) mouseY - (topPos + TRADE_BUTTON_Y);
        if (mx >= 0 && my >= 0 && mx < TRADE_BUTTON_SIZE && my < TRADE_BUTTON_SIZE && menu.canTrade()) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MenuPech.TRADE_BUTTON_ID);
            if (minecraft.player != null) {
                minecraft
                        .getSoundManager()
                        .play(SimpleSoundInstance.forUI(
                                TTSounds.PECH_DICE.get(),
                                0.95F + minecraft.player.getRandom().nextFloat() * 0.1F,
                                DICE_VOLUME));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
