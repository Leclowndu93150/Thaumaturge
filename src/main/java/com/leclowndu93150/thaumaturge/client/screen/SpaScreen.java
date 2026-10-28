package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.spa.BlockEntitySpa;
import com.leclowndu93150.thaumaturge.content.spa.MenuSpa;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

public class SpaScreen extends AbstractTTContainerScreen<MenuSpa> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/gui/gui_spa.png");

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
    private static final int FLUID_HEIGHT = 48;
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

    private @Nullable List<Component> tooltipLines;

    public SpaScreen(MenuSpa menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, 176, 166);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        tooltipLines = null;
        super.render(graphics, mouseX, mouseY, partialTick);
        if (tooltipLines != null) {
            graphics.renderComponentTooltip(font, tooltipLines, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        BlockEntitySpa spa = menu.blockEntity();
        if (spa == null) {
            return;
        }
        int x = leftPos;
        int y = topPos;
        boolean mix = spa.getMix();
        graphics.blit(
                TEXTURE, x + MIX_X, y + MIX_Y, MIX_ON_U, mix ? MIX_ON_V : MIX_OFF_V, MIX_SIZE, MIX_SIZE, 256, 256);
        graphics.blit(TEXTURE, x + GAUGE_X, y + GAUGE_Y, GAUGE_U, GAUGE_V, GAUGE_WIDTH, GAUGE_HEIGHT, 256, 256);

        FluidStack resource = spa.getTank().getFluid();
        int amount = spa.getTank().getFluidAmount();
        if (amount > 0 && !resource.isEmpty()) {
            Fluid fluid = resource.getFluid();
            IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid);
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(ext.getStillTexture(resource));
            int tint = spa.getLevel() instanceof ClientLevel level
                    ? ext.getTintColor(fluid.defaultFluidState(), level, spa.getBlockPos())
                    : ext.getTintColor(resource);
            float r = ARGB32.red(tint) / 255.0F;
            float g = ARGB32.green(tint) / 255.0F;
            float b = ARGB32.blue(tint) / 255.0F;
            float a = ARGB32.alpha(tint) / 255.0F;
            int fluidHeight = (Math.clamp(amount, 0, BlockEntitySpa.TANK_CAPACITY) * FLUID_HEIGHT
                            + BlockEntitySpa.TANK_CAPACITY / 2)
                    / BlockEntitySpa.TANK_CAPACITY;
            for (int remaining = fluidHeight; remaining > 0; ) {
                int segmentHeight = Math.min(FLUID_WIDTH, remaining);
                graphics.blit(
                        x + FLUID_X,
                        y + FLUID_Y + FLUID_HEIGHT - remaining,
                        0,
                        FLUID_WIDTH,
                        segmentHeight,
                        sprite,
                        r,
                        g,
                        b,
                        a);
                remaining -= segmentHeight;
            }
        }
        int tankX = mouseX - (x + TANK_HOVER_X);
        int tankY = mouseY - (y + TANK_HOVER_Y);
        if (tankX >= 0
                && tankY >= 0
                && tankX < TANK_HOVER_WIDTH
                && tankY < TANK_HOVER_HEIGHT
                && amount > 0
                && !resource.isEmpty()) {
            List<Component> lines = new ArrayList<>();
            lines.add(resource.getFluid().getFluidType().getDescription());
            lines.add(Component.translatable("gui.thaumaturge.fluid_amount", amount));
            tooltipLines = lines;
        }

        int mixX = mouseX - (x + MIX_HOVER_X);
        int mixY = mouseY - (y + MIX_HOVER_Y);
        if (mixX >= 0 && mixY >= 0 && mixX < MIX_HOVER_SIZE && mixY < MIX_HOVER_SIZE) {
            tooltipLines = List.of(
                    Component.translatable(mix ? "gui.thaumaturge.spa.mix.true" : "gui.thaumaturge.spa.mix.false"));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX - (leftPos + MIX_X);
        int my = (int) mouseY - (topPos + MIX_Y);
        if (mx >= 0 && my >= 0 && mx < MIX_SIZE && my < MIX_SIZE) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MenuSpa.MIX_BUTTON_ID);
            if (minecraft.player != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(TTSounds.CLACK.get(), 1.0F, CLICK_VOLUME));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
