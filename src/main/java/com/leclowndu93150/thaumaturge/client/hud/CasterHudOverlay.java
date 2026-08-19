package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.text.DecimalFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.gui.GuiLayer;

public final class CasterHudOverlay implements GuiLayer {
    private static final Identifier HUD = TCIds.rl("textures/gui/hud.png");
    private static final Identifier HUD_WAND = TCIds.rl("textures/gui/hud_wand.png");

    private static final int[] previousVis = new int[WandEconomy.PRIMAL_COUNT];
    private static long changeSyncTime;
    private static final int TEX_SIZE = 256;

    public static final int STACK_HEIGHT = 60;
    private static final int DIAL_SIZE = 32;
    private static final int DIAL_SRC_SIZE = 64;
    private static final int ANCHOR = 16;
    private static final float SPOKE_BASE_DEG = -15.0F;
    private static final float SPOKE_STEP_DEG = 24.0F;
    private static final int SPOKE_RADIUS = -32;
    private static final int MARKER_SIZE = 8;
    private static final int MARKER_HALF = 4;
    private static final float COST_MARKER_U = 136.0F;
    private static final float RISE_MARKER_U = 120.0F;
    private static final float FALL_MARKER_U = 128.0F;
    private static final int COST_TEXT_X = 8;
    private static final long CHANGE_SYNC_INTERVAL_MS = 1000L;
    private static final float BAR_SPACE_SCALE = 0.5F;
    private static final int BAR_MAX_HEIGHT = 30;
    private static final int BAR_BOTTOM = 35;
    private static final float BAR_FILL_U = 104.0F;
    private static final int BAR_FILL_W = 8;
    private static final int BAR_FRAME_X = -8;
    private static final int BAR_FRAME_Y = -3;
    private static final float BAR_FRAME_U = 72.0F;
    private static final int BAR_FRAME_W = 16;
    private static final int BAR_FRAME_H = 42;
    private static final float BAR_FILL_ALPHA = 0.8F;
    private static final int AMOUNT_TEXT_X = -32;
    private static final int AMOUNT_TEXT_Y = -4;
    private static final int ITEM_HALF = 8;
    private static final int COUNT_TEXT_LIFT = 9;
    private static final int COUNT_TEXT_X = 16;
    private static final int COUNT_TEXT_Y = 24;
    private static final float COUNT_TEXT_SCALE = 0.5F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int OUTLINE_BLACK = 0xFF000000;
    private static final int DEFAULT_ENERGY_COLOR = 0xC0FFFF;

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#######.#");

    public CasterHudOverlay() {}

    public static boolean isVisible(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || !mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        return player.getMainHandItem().getItem() instanceof ICaster || player.getOffhandItem().getItem() instanceof ICaster;
    }

    public static LeftHudStack.Gauge dialGauge() {
        return new LeftHudStack.Gauge() {
            @Override
            public boolean visible(Minecraft mc, LocalPlayer player) {
                return isVisible(mc) && !ThaumaturgeClientConfig.dialBottom();
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
                renderDial(graphics, 0);
            }
        };
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!isVisible(mc) || !ThaumaturgeClientConfig.dialBottom()) {
            return;
        }
        renderDial(graphics, graphics.guiHeight() - DIAL_SIZE);
    }

    private static void renderDial(GuiGraphicsExtractor graphics, int dialY) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ItemStack casterStack = player.getMainHandItem();
        if (!(casterStack.getItem() instanceof ICaster)) {
            casterStack = player.getOffhandItem();
        }
        ICaster wand = (ICaster) casterStack.getItem();

        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD_WAND, 0, dialY, 0.0F, 0.0F, DIAL_SIZE, DIAL_SIZE, DIAL_SRC_SIZE, DIAL_SRC_SIZE, TEX_SIZE, TEX_SIZE);

        int max = WandVisHelper.getMaxVis(casterStack);
        ItemStack focusStack = wand.getFocusStack(casterStack);
        boolean hasFocus = focusStack.getItem() instanceof ItemFocus;
        float perAspectCost = 0.0F;
        if (hasFocus && focusStack.getItem() instanceof ItemFocus focus && focus.getVisCost(focusStack) > 0.0F) {
            perAspectCost = focus.getVisCost(focusStack) * wand.getConsumptionModifier(casterStack, player, false) / WandEconomy.PRIMAL_COUNT;
        }
        boolean sneak = player.isShiftKeyDown();
        long now = Util.getMillis();
        boolean snapshot = now >= changeSyncTime;
        if (snapshot) {
            changeSyncTime = now + CHANGE_SYNC_INTERVAL_MS;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(ANCHOR, dialY + ANCHOR);
        int count = 0;
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            int amt = WandVisHelper.getVis(casterStack, primal);
            graphics.pose().pushMatrix();
            if (!ThaumaturgeClientConfig.dialBottom()) {
                graphics.pose().rotate((float) Math.toRadians(90.0));
            }
            graphics.pose().rotate((float) Math.toRadians(SPOKE_BASE_DEG + count * SPOKE_STEP_DEG));
            graphics.pose().translate(0.0F, SPOKE_RADIUS);
            graphics.pose().scale(BAR_SPACE_SCALE, BAR_SPACE_SCALE);
            int loc = max > 0 ? (int) (BAR_MAX_HEIGHT * (float) amt / max) : 0;
            if (loc > 0) {
                int color = primalColor(mc, primal);
                graphics.blit(RenderPipelines.GUI_TEXTURED, HUD_WAND, -BAR_FILL_W / 2, BAR_BOTTOM - loc, BAR_FILL_U, 0.0F, BAR_FILL_W, loc, BAR_FILL_W, loc, TEX_SIZE, TEX_SIZE,
                        ARGB.color(Math.round(BAR_FILL_ALPHA * 255.0F), color));
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, HUD_WAND, BAR_FRAME_X, BAR_FRAME_Y, BAR_FRAME_U, 0.0F, BAR_FRAME_W, BAR_FRAME_H, BAR_FRAME_W, BAR_FRAME_H, TEX_SIZE, TEX_SIZE);
            int markerShift = 0;
            if (perAspectCost > 0.0F) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, HUD_WAND, -MARKER_HALF, -MARKER_SIZE, COST_MARKER_U, 0.0F, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, TEX_SIZE, TEX_SIZE);
                markerShift = MARKER_SIZE;
            }
            if (previousVis[count] > amt) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, HUD_WAND, -MARKER_HALF, -MARKER_SIZE - markerShift, FALL_MARKER_U, 0.0F, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, TEX_SIZE,
                        TEX_SIZE);
            } else if (previousVis[count] < amt) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, HUD_WAND, -MARKER_HALF, -MARKER_SIZE - markerShift, RISE_MARKER_U, 0.0F, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, TEX_SIZE,
                        TEX_SIZE);
            }
            if (snapshot) {
                previousVis[count] = amt;
            }
            if (sneak) {
                graphics.pose().pushMatrix();
                graphics.pose().rotate((float) Math.toRadians(-90.0));
                graphics.text(mc.font, AMOUNT_FORMAT.format(amt / (float) WandEconomy.CENTIVIS_PER_VIS), AMOUNT_TEXT_X, AMOUNT_TEXT_Y, WHITE, false);
                graphics.pose().popMatrix();
                if (perAspectCost > 0.0F) {
                    graphics.pose().pushMatrix();
                    graphics.pose().rotate((float) Math.toRadians(-90.0));
                    graphics.text(mc.font, AMOUNT_FORMAT.format(perAspectCost), COST_TEXT_X, AMOUNT_TEXT_Y, WHITE, false);
                    graphics.pose().popMatrix();
                }
            }
            graphics.pose().popMatrix();
            count++;
        }
        graphics.pose().popMatrix();

        if (hasFocus) {
            BlockState picked = wand.getPickedBlock(player.getMainHandItem());
            ItemStack pickedStack = picked == null ? ItemStack.EMPTY : new ItemStack(picked.getBlock().asItem());
            if (!pickedStack.isEmpty()) {
                renderTradeHud(graphics, mc, player, pickedStack, dialY);
            } else {
                graphics.item(focusStack, ANCHOR - ITEM_HALF, dialY + ANCHOR - ITEM_HALF);
            }
        }
    }

    private static int primalColor(Minecraft mc, ResourceKey<IAspect> primal) {
        if (mc.level == null) {
            return DEFAULT_ENERGY_COLOR;
        }
        return mc.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(primal).value().color();
    }

    private static void renderTradeHud(GuiGraphicsExtractor graphics, Minecraft mc, LocalPlayer player, ItemStack picked, int dialY) {
        int amount = 0;
        NonNullList<ItemStack> main = player.getInventory().getNonEquipmentItems();
        for (ItemStack stack : main) {
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, picked)) {
                amount += stack.getCount();
            }
        }
        graphics.item(picked, ANCHOR - ITEM_HALF, dialY + ANCHOR - ITEM_HALF);
        String text = Integer.toString(amount);
        int width = mc.font.width(text);
        graphics.pose().pushMatrix();
        graphics.pose().translate(ANCHOR, dialY + ANCHOR - COUNT_TEXT_LIFT);
        graphics.pose().scale(COUNT_TEXT_SCALE, COUNT_TEXT_SCALE);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if ((a == 0 || b == 0) && (a != 0 || b != 0)) {
                    graphics.text(mc.font, text, a + COUNT_TEXT_X - width, b + COUNT_TEXT_Y, OUTLINE_BLACK, false);
                }
            }
        }
        graphics.text(mc.font, text, COUNT_TEXT_X - width, COUNT_TEXT_Y, WHITE, false);
        graphics.pose().popMatrix();
    }
}
