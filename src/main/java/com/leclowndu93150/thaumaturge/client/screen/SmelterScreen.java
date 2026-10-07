package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTGauge;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.MenuSmelter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class SmelterScreen extends AbstractTTContainerScreen<MenuSmelter> {
    private static final Identifier SHEET = Identifier.fromNamespaceAndPath(TTIds.MODID, "textures/gui/gui_smelter.png");
    private static final int SHEET_SIZE = 256;
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 166;

    private static final TTGauge FLAME = new TTGauge(SHEET, SHEET_SIZE, 176, 0, 16, 20);
    private static final int FLAME_X = 80;
    private static final int FLAME_Y = 26;

    private static final TTGauge PROGRESS = new TTGauge(SHEET, SHEET_SIZE, 216, 0, 9, 46);
    private static final int PROGRESS_X = 106;
    private static final int PROGRESS_Y = 13;

    private static final TTGauge VIS = new TTGauge(SHEET, SHEET_SIZE, 200, 0, 8, 48);
    private static final int VIS_X = 61;
    private static final int VIS_Y = 12;

    private static final TTGauge VIS_GLASS = new TTGauge(SHEET, SHEET_SIZE, 232, 0, 10, 55);
    private static final int VIS_GLASS_X = 60;
    private static final int VIS_GLASS_Y = 8;

    SmelterScreen(MenuSmelter menu, Inventory inventory, Component title) {
        super(menu, inventory, title, SHEET, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BlockEntitySmelter smelter = menu.blockEntity();
        if (smelter == null) {
            return;
        }
        FLAME.extractRising(graphics, leftPos + FLAME_X, topPos + FLAME_Y, smelter.getBurnTimeRemainingScaled(FLAME.height()));
        PROGRESS.extractRising(graphics, leftPos + PROGRESS_X, topPos + PROGRESS_Y, smelter.getCookProgressScaled(PROGRESS.height()));
        VIS.extractRising(graphics, leftPos + VIS_X, topPos + VIS_Y, smelter.getVisScaled(VIS.height()));
        VIS_GLASS.extractFull(graphics, leftPos + VIS_GLASS_X, topPos + VIS_GLASS_Y);
    }
}
