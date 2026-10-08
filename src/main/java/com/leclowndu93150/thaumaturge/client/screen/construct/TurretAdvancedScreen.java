package com.leclowndu93150.thaumaturge.client.screen.construct;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbowAdvanced;
import com.leclowndu93150.thaumaturge.content.entity.construct.MenuTurretAdvanced;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class TurretAdvancedScreen extends TurretBasicScreen<MenuTurretAdvanced> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/gui/gui_turret_advanced.png");
    private static final int HEALTH_BAR_X = 30;
    private static final int BUTTON_X = 90;
    private static final int BUTTON_FIRST_Y = 13;
    private static final int BUTTON_SPACING = 14;
    private static final int BUTTON_SIZE = 8;
    private static final int BUTTON_U = 192;
    private static final int BUTTON_V = 16;
    private static final int BUTTON_TOGGLED_V = 24;
    private static final int LABEL_OFFSET_X = 12;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final float CLICK_VOLUME = 0.4F;
    private static final String[] BUTTON_LABELS = {
        "gui.thaumaturge.turret.target.1",
        "gui.thaumaturge.turret.target.2",
        "gui.thaumaturge.turret.target.3",
        "gui.thaumaturge.turret.target.4"
    };

    public TurretAdvancedScreen(MenuTurretAdvanced menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawHealthBar(graphics, HEALTH_BAR_X, TEXTURE);
        EntityTurretCrossbowAdvanced turret = advancedTurret();
        if (turret == null) {
            return;
        }
        boolean[] toggles = {
            turret.getTargetAnimal(), turret.getTargetMob(), turret.getTargetPlayer(), turret.getTargetFriendly()
        };
        for (int index = 0; index < toggles.length; index++) {
            int x = leftPos + BUTTON_X;
            int y = topPos + BUTTON_FIRST_Y + index * BUTTON_SPACING;
            graphics.blit(TEXTURE, x, y, BUTTON_U, BUTTON_V, BUTTON_SIZE, BUTTON_SIZE, 256, 256);
            if (toggles[index]) {
                graphics.blit(TEXTURE, x, y, BUTTON_U, BUTTON_TOGGLED_V, BUTTON_SIZE, BUTTON_SIZE, 256, 256);
            }
            graphics.drawString(
                    font, Component.translatable(BUTTON_LABELS[index]), x + LABEL_OFFSET_X, y, LABEL_COLOR, false);
        }
    }

    private EntityTurretCrossbowAdvanced advancedTurret() {
        return menu.advancedTurret();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX - (leftPos + BUTTON_X);
        int my = (int) mouseY - (topPos + BUTTON_FIRST_Y);
        if (mx >= 0 && mx < BUTTON_SIZE && my >= 0) {
            int index = my / BUTTON_SPACING;
            if (index < 4 && my - index * BUTTON_SPACING < BUTTON_SIZE) {
                minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId, MenuTurretAdvanced.BUTTON_ANIMAL + index);
                if (minecraft.player != null) {
                    minecraft
                            .getSoundManager()
                            .play(SimpleSoundInstance.forUI(TTSounds.CLACK.get(), 1.0F, CLICK_VOLUME));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
