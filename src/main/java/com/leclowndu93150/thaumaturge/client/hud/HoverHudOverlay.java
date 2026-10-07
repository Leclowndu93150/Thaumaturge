package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;

public final class HoverHudOverlay implements GuiLayer {
    private static final int FILL_X = 6;
    private static final int FILL_BOTTOM_OFFSET = 24;
    private static final int FILL_U = 0;
    private static final int FILL_V = 72;
    private static final int FILL_W = 8;
    private static final int FILL_MAX_H = 48;
    private static final int FILL_TINT = ARGB.colorFromFloat(1.0F, 0.0F, 1.0F, 0.75F);
    private static final int FRAME_X = 4;
    private static final int FRAME_TOP_OFFSET = 28;
    private static final int FRAME_U = 14;
    private static final int FRAME_V = 72;
    private static final int FRAME_W = 12;
    private static final int FRAME_H = 56;
    private static final int ICON_X = 2;
    private static final int ICON_TOP_OFFSET = 43;
    private static final int SPARK_SIZE = 16;
    private static final int SPARK_FRAMES = 14;
    private static final int SPARK_STRIP_W = SPARK_SIZE * SPARK_FRAMES;
    private static final int SPARK_TINT = ARGB.colorFromFloat(0.66F, 1.0F, 1.0F, 1.0F);

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || mc.screen != null) {
            return;
        }
        ItemStack worn = HoverManager.wornHoverGear(player);
        if (!(worn.getItem() instanceof IHoverGear gear)) {
            return;
        }
        int middle = graphics.guiHeight() / 2;
        int fill = Math.min(FILL_MAX_H, Math.round((float) gear.getHoverFuel(worn) / gear.getMaxHoverFuel(worn) * FILL_MAX_H));
        if (fill > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, FILL_X, middle + FILL_BOTTOM_OFFSET - fill, FILL_U, FILL_V + FILL_MAX_H - fill, FILL_W, fill, FILL_W, fill,
                    TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE, FILL_TINT);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, FRAME_X, middle - FRAME_TOP_OFFSET, FRAME_U, FRAME_V, FRAME_W, FRAME_H, FRAME_W, FRAME_H, TTScreenTextures.TEX_SIZE,
                TTScreenTextures.TEX_SIZE);
        if (HoverManager.isHovering(player)) {
            int frame = player.tickCount % SPARK_FRAMES;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD_HOVER_SPARK, ICON_X, middle - ICON_TOP_OFFSET, frame * SPARK_SIZE, 0.0F, SPARK_SIZE, SPARK_SIZE, SPARK_SIZE, SPARK_SIZE,
                    SPARK_STRIP_W, SPARK_SIZE, SPARK_TINT);
        }
        graphics.item(worn, ICON_X, middle - ICON_TOP_OFFSET);
    }
}
