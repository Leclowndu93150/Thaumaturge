package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.aura.ClientAuraCache;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.network.ServerboundRequestAuraChunkPayload;
import java.text.DecimalFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class AuraHudOverlay implements LeftHudStack.Gauge {
    private static final Identifier HUD = TCIds.rl("textures/gui/hud.png");
    private static final int TEX_SIZE = 256;

    private static final float VISCON = 525.0F;
    private static final int HUD_X = 2;
    private static final int BAR_X = 5;
    private static final float BAR_TOP = 10.0F;
    private static final int BAR_RANGE = 64;
    private static final float FRAME_U = 72.0F;
    private static final float FRAME_V = 48.0F;
    private static final int FRAME_W = 16;
    private static final int FRAME_H = 80;
    private static final float FILL_U = 88.0F;
    private static final float FILL_V = 56.0F;
    private static final int FILL_W = 8;
    private static final int FILL_H = 64;
    private static final float VIS_RIPPLE_U = 96.0F;
    private static final float FLUX_RIPPLE_U = 104.0F;
    private static final float RIPPLE_V_BASE = 56.0F;
    private static final float FLUX_RIPPLE_V_BASE = 120.0F;
    private static final float NEEDLE_TOP = 8.0F;
    private static final float NEEDLE_U = 117.0F;
    private static final float NEEDLE_V = 61.0F;
    private static final int NEEDLE_W = 14;
    private static final int NEEDLE_H = 5;
    private static final int NEEDLE_X = 2;
    private static final int FRAME_X = 1;
    private static final int FRAME_Y = 1;
    private static final int TEXT_X = 16;
    private static final float TEXT_SCALE = 0.5F;

    private static final int VIS_FILL_TINT = 0xFFB266E5;
    private static final int FLUX_FILL_TINT = 0xFF3F194C;
    private static final int VIS_RIPPLE_TINT = 0x80FFFFFF;
    private static final int FLUX_RIPPLE_TINT = 0x80B266FF;
    private static final int VIS_TEXT_COLOR = 0xFFEEAAFF;
    private static final int FLUX_TEXT_COLOR = 0xFFAA11BB;
    private static final int STACK_HEIGHT = 83;

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#######.#");

    public AuraHudOverlay() {}

    @Override
    public boolean visible(Minecraft mc, LocalPlayer player) {
        return shouldShow(player);
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
        ClientAuraCache.tick();
        ChunkPos pos = ChunkPos.containing(player.blockPosition());
        if (ClientAuraCache.shouldRequest(pos)) {
            ClientPacketDistributor.sendToServer(new ServerboundRequestAuraChunkPayload(pos.x(), pos.z()));
        }
        ClientAuraCache.Snapshot snap = ClientAuraCache.get(pos);
        if (snap == null) {
            snap = ClientAuraCache.latest();
        }
        if (snap == null) {
            return;
        }

        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        float base = Mth.clamp(snap.base() / VISCON, 0.0F, 1.0F);
        float vis = Mth.clamp(snap.vis() / VISCON, 0.0F, 1.0F);
        float flux = Mth.clamp(snap.flux() / VISCON, 0.0F, 1.0F);
        float count = player.tickCount + partial;
        float count2 = player.tickCount / 3.0F + partial;
        if (flux + vis > 1.0F) {
            float m = 1.0F / (flux + vis);
            base *= m;
            vis *= m;
            flux *= m;
        }

        if (vis > 0.0F) {
            float start = BAR_TOP + (1.0F - vis) * BAR_RANGE;
            int fillHeight = Math.round(vis * BAR_RANGE);
            int y = Math.round(BAR_TOP + BAR_RANGE) - fillHeight;
            graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, HUD_X + BAR_X, y, FILL_U, FILL_V, FILL_W, fillHeight, FILL_W, FILL_H, TEX_SIZE, TEX_SIZE, VIS_FILL_TINT);
            graphics.blit(TCRenderPipelines.GUI_TEXTURED_ADDITIVE, HUD, HUD_X + BAR_X, y, VIS_RIPPLE_U, RIPPLE_V_BASE + count % BAR_RANGE, FILL_W, fillHeight, FILL_W, fillHeight, TEX_SIZE, TEX_SIZE,
                    VIS_RIPPLE_TINT);
            if (player.isShiftKeyDown()) {
                drawAmount(graphics, AMOUNT_FORMAT.format(snap.vis()), start, VIS_TEXT_COLOR);
            }
        }

        if (flux > 0.0F) {
            float start = BAR_TOP + (1.0F - flux - vis) * BAR_RANGE;
            int fillHeight = Math.round(flux * BAR_RANGE);
            int y = Math.round(start);
            graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, HUD_X + BAR_X, y, FILL_U, FILL_V, FILL_W, fillHeight, FILL_W, FILL_H, TEX_SIZE, TEX_SIZE, FLUX_FILL_TINT);
            graphics.blit(TCRenderPipelines.GUI_TEXTURED_ADDITIVE, HUD, HUD_X + BAR_X, y, FLUX_RIPPLE_U, FLUX_RIPPLE_V_BASE - count2 % BAR_RANGE, FILL_W, fillHeight, FILL_W, fillHeight, TEX_SIZE,
                    TEX_SIZE, FLUX_RIPPLE_TINT);
            if (player.isShiftKeyDown()) {
                drawAmount(graphics, AMOUNT_FORMAT.format(snap.flux()), start - 4.0F, FLUX_TEXT_COLOR);
            }
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, HUD_X + FRAME_X, FRAME_Y, FRAME_U, FRAME_V, FRAME_W, FRAME_H, FRAME_W, FRAME_H, TEX_SIZE, TEX_SIZE);

        float needleStart = NEEDLE_TOP + (1.0F - base) * BAR_RANGE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, HUD_X + NEEDLE_X, Math.round(needleStart), NEEDLE_U, NEEDLE_V, NEEDLE_W, NEEDLE_H, NEEDLE_W, NEEDLE_H, TEX_SIZE, TEX_SIZE);
    }

    private static void drawAmount(GuiGraphicsExtractor graphics, String text, float y, int color) {
        Minecraft mc = Minecraft.getInstance();
        graphics.pose().pushMatrix();
        graphics.pose().translate(HUD_X + TEXT_X, y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(mc.font, text, 0, 0, color, false);
        graphics.pose().popMatrix();
    }

    private static boolean shouldShow(Player player) {
        if (GogglesAccess.wearsGoggles(player)) {
            return true;
        }
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty() && main.getItem() instanceof ThaumometerItem) {
            return true;
        }
        ItemStack off = player.getOffhandItem();
        return !off.isEmpty() && off.getItem() instanceof ThaumometerItem;
    }
}
