package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.MenuSmelter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SmelterScreen extends AbstractTCContainerScreen<MenuSmelter> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_smelter.png");

    protected SmelterScreen(MenuSmelter menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, 176, 166);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        if (menu.blockEntity() != null) {
            BlockEntitySmelter smelter = menu.blockEntity();
            int scaledBurnTime = smelter.getBurnTimeRemainingScaled(20);
            if (scaledBurnTime > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 80, y + 26 + 20 - scaledBurnTime, 176, 20 - scaledBurnTime, 16, scaledBurnTime, 256, 256);
            }

            int scaledCookTime = smelter.getCookProgressScaled(46);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 106, y + 13 + 46 - scaledCookTime, 216, 46 - scaledCookTime, 9, scaledCookTime, 256, 256);
            int visScaled = smelter.getVisScaled(46);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 61, y + 12 + 48 - visScaled, 200, 48 - visScaled, 8, visScaled, 256, 256);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 60, y + 8, 232, 0, 10, 55, 256, 256);
        }
    }
}
