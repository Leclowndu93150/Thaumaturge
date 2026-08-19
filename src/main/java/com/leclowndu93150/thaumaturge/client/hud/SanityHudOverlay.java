package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;

public final class SanityHudOverlay implements LeftHudStack.Gauge {
    private static final Identifier HUD = TCIds.rl("textures/gui/hud.png");
    private static final int TEX_SIZE = 256;
    private static final int HUD_X = 1;
    private static final int HUD_Y = 1;
    private static final int FRAME_U = 152;
    private static final int OVERLAY_U = 176;
    private static final int FRAME_V = 0;
    private static final int FRAME_W = 20;
    private static final int FRAME_H = 76;
    private static final int FILL_U = 200;
    private static final int FILL_X = 7;
    private static final int FILL_TOP = 21;
    private static final int FILL_W = 8;
    private static final int FILL_RANGE = 48;
    private static final float WARP_CAP = 100.0F;
    private static final int TEMPORARY_TINT = ARGB.colorFromFloat(1.0F, 1.0F, 0.5F, 1.0F);
    private static final int NORMAL_TINT = ARGB.colorFromFloat(1.0F, 0.75F, 0.0F, 0.75F);
    private static final int PERMANENT_TINT = ARGB.colorFromFloat(1.0F, 0.5F, 0.0F, 0.5F);
    private static final int STACK_HEIGHT = 78;

    @Override
    public boolean visible(Minecraft mc, LocalPlayer player) {
        return holds(player, true) || holds(player, false);
    }

    @Override
    public int height() {
        return STACK_HEIGHT;
    }

    @Override
    public String exclusiveGroup() {
        return null;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, HUD_X, HUD_Y, FRAME_U, FRAME_V, FRAME_W, FRAME_H, FRAME_W, FRAME_H, TEX_SIZE, TEX_SIZE);
        IPlayerWarp warp = WarpHelper.getWarp(player);
        int permanent = warp.get(WarpType.PERMANENT);
        int normal = warp.get(WarpType.NORMAL);
        int temporary = warp.get(WarpType.TEMPORARY);
        float total = permanent + normal + temporary;
        float mod = 1.0F;
        if (total > WARP_CAP) {
            mod = WARP_CAP / total;
            total = WARP_CAP;
        }
        int gap = (int) ((WARP_CAP - total) / WARP_CAP * FILL_RANGE);
        int temporaryHeight = (int) (temporary / WARP_CAP * FILL_RANGE * mod);
        int normalHeight = (int) (normal / WARP_CAP * FILL_RANGE * mod);
        if (temporary > 0) {
            fill(graphics, gap, temporaryHeight + gap, TEMPORARY_TINT);
        }
        if (normal > 0) {
            fill(graphics, temporaryHeight + gap, temporaryHeight + normalHeight + gap, NORMAL_TINT);
        }
        if (permanent > 0) {
            fill(graphics, temporaryHeight + normalHeight + gap, FILL_RANGE, PERMANENT_TINT);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, HUD_X, HUD_Y, OVERLAY_U, FRAME_V, FRAME_W, FRAME_H, FRAME_W, FRAME_H, TEX_SIZE, TEX_SIZE);
    }

    private static void fill(GuiGraphicsExtractor graphics, int from, int to, int tint) {
        int height = to - from;
        if (height <= 0) {
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, HUD_X + FILL_X - 1, HUD_Y + FILL_TOP - 1 + from, FILL_U, from, FILL_W, height, FILL_W, height, TEX_SIZE, TEX_SIZE, tint);
    }

    private static boolean holds(Player player, boolean main) {
        return (main ? player.getMainHandItem() : player.getOffhandItem()).is(TCItems.SANITY_CHECKER.get());
    }
}
