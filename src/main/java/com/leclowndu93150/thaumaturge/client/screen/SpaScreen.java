package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.spa.BlockEntitySpa;
import com.leclowndu93150.thaumaturge.content.spa.MenuSpa;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class SpaScreen extends AbstractTCContainerScreen<MenuSpa> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_spa.png");

    private static final int MIX_X = 89;
    private static final int MIX_Y = 35;
    private static final int MIX_SIZE = 8;
    private static final int MIX_ON_U = 208;
    private static final int MIX_ON_V = 16;
    private static final int MIX_OFF_V = 32;
    private static final int MIX_HOVER_X = 88;
    private static final int MIX_HOVER_Y = 34;
    private static final int MIX_HOVER_SIZE = 10;
    private static final int FLUID_X = 107;
    private static final int FLUID_Y = 15;
    private static final int FLUID_WIDTH = 8;
    private static final int FLUID_SEGMENTS = 6;
    private static final int FLUID_HEIGHT = 48;
    private static final int COVER_U = 107;
    private static final int COVER_V = 15;
    private static final int COVER_WIDTH = 10;
    private static final int GAUGE_X = 106;
    private static final int GAUGE_Y = 11;
    private static final int GAUGE_U = 232;
    private static final int GAUGE_V = 0;
    private static final int GAUGE_WIDTH = 10;
    private static final int GAUGE_HEIGHT = 55;
    private static final int TANK_HOVER_X = 104;
    private static final int TANK_HOVER_Y = 10;
    private static final int TANK_HOVER_WIDTH = 10;
    private static final int TANK_HOVER_HEIGHT = 55;
    private static final float CLICK_VOLUME = 0.4F;

    public SpaScreen(MenuSpa menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, 176, 166);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BlockEntitySpa spa = menu.blockEntity();
        if (spa == null) {
            return;
        }
        int x = leftPos;
        int y = topPos;
        boolean mix = spa.getMix();
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + MIX_X, y + MIX_Y, MIX_ON_U, mix ? MIX_ON_V : MIX_OFF_V, MIX_SIZE, MIX_SIZE, 256, 256);

        FluidResource resource = spa.getTank().getResource(0);
        int amount = spa.getTank().getAmountAsInt(0);
        if (amount > 0 && !resource.isEmpty()) {
            Fluid fluid = resource.getFluid();
            FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
            TextureAtlasSprite sprite = model.stillMaterial().sprite();
            int tint = -1;
            if (model.tintSource() != null) {
                BlockState fluidBlock = fluid.defaultFluidState().createLegacyBlock();
                tint = ARGB.opaque(model.tintSource().color(fluidBlock));
            }
            for (int a = 0; a < FLUID_SEGMENTS; a++) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + FLUID_X, y + FLUID_Y + a * FLUID_WIDTH, FLUID_WIDTH, FLUID_WIDTH, tint);
            }
            float bar = (float) amount / BlockEntitySpa.TANK_CAPACITY;
            int coverHeight = (int) (FLUID_HEIGHT - FLUID_HEIGHT * bar);
            if (coverHeight > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + FLUID_X, y + FLUID_Y, COVER_U, COVER_V, COVER_WIDTH, coverHeight, 256, 256);
            }
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + GAUGE_X, y + GAUGE_Y, GAUGE_U, GAUGE_V, GAUGE_WIDTH, GAUGE_HEIGHT, 256, 256);

        int tankX = mouseX - (x + TANK_HOVER_X);
        int tankY = mouseY - (y + TANK_HOVER_Y);
        if (tankX >= 0 && tankY >= 0 && tankX < TANK_HOVER_WIDTH && tankY < TANK_HOVER_HEIGHT && amount > 0 && !resource.isEmpty()) {
            List<Component> lines = new ArrayList<>();
            lines.add(resource.getFluid().getFluidType().getDescription());
            lines.add(Component.literal(amount + " mb"));
            graphics.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
        }

        int mixX = mouseX - (x + MIX_HOVER_X);
        int mixY = mouseY - (y + MIX_HOVER_Y);
        if (mixX >= 0 && mixY >= 0 && mixX < MIX_HOVER_SIZE && mixY < MIX_HOVER_SIZE) {
            graphics.setTooltipForNextFrame(font, Component.translatable(mix ? "gui.thaumaturge.spa.mix.true" : "gui.thaumaturge.spa.mix.false"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mx = (int) event.x() - (leftPos + MIX_X);
        int my = (int) event.y() - (topPos + MIX_Y);
        if (mx >= 0 && my >= 0 && mx < MIX_SIZE && my < MIX_SIZE) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MenuSpa.MIX_BUTTON_ID);
            if (minecraft.player != null) {
                minecraft.player.playSound(TCSounds.CLACK.get(), CLICK_VOLUME, 1.0F);
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
