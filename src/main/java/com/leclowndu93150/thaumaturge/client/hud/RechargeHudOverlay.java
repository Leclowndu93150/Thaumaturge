package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.items.IRechargable;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;

public final class RechargeHudOverlay implements GuiLayer {
    private static final Identifier HUD = TCIds.rl("textures/gui/hud.png");
    private static final int TEX_SIZE = 256;

    private static final int PERIODIC_SHOW_TICKS = 60;
    private static final int ANCHOR_X = 4;
    private static final int ANCHOR_BOTTOM = 48;
    private static final int ENTRY_SPACING = 26;
    private static final int ITEM_SIZE = 16;
    private static final int BAR_GAP_X = 3;
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
    private static final int AMOUNT_TEXT_Y = -14;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DEFAULT_ENERGY_COLOR = 0xC0FFFF;

    private final Map<EquipmentSlot, Integer> lastCharge = new EnumMap<>(EquipmentSlot.class);
    private final Map<EquipmentSlot, Integer> changeTick = new EnumMap<>(EquipmentSlot.class);

    public RechargeHudOverlay() {}

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        int shown = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof IRechargable rechargable)) {
                lastCharge.remove(slot);
                changeTick.remove(slot);
                continue;
            }
            IRechargable.ChargeDisplay display = rechargable.showInHud(stack, player);
            if (display == IRechargable.ChargeDisplay.NEVER) {
                continue;
            }
            int charge = RechargeAccess.getCharge(stack);
            Integer previous = lastCharge.put(slot, charge);
            if (previous == null || previous != charge) {
                changeTick.put(slot, player.tickCount);
            }
            boolean held = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
            if (!held && display == IRechargable.ChargeDisplay.PERIODIC && player.tickCount - changeTick.getOrDefault(slot, Integer.MIN_VALUE) > PERIODIC_SHOW_TICKS) {
                continue;
            }
            drawMeter(graphics, mc, stack, rechargable.getMaxCharge(stack, player), charge, shown++, player.isShiftKeyDown());
        }
    }

    private static void drawMeter(GuiGraphicsExtractor graphics, Minecraft mc, ItemStack stack, int max, int charge, int index, boolean showAmount) {
        int y = graphics.guiHeight() - ANCHOR_BOTTOM - index * ENTRY_SPACING;
        graphics.item(stack, ANCHOR_X, y - ITEM_SIZE);
        graphics.pose().pushMatrix();
        graphics.pose().translate(ANCHOR_X + ITEM_SIZE + BAR_GAP_X + BAR_FRAME_W * BAR_SPACE_SCALE / 2.0F, y - ITEM_SIZE + BAR_FRAME_Y * BAR_SPACE_SCALE);
        graphics.pose().scale(BAR_SPACE_SCALE, BAR_SPACE_SCALE);
        int loc = max > 0 ? (int) (BAR_MAX_HEIGHT * (float) charge / max) : 0;
        if (loc > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, -BAR_FILL_W / 2, BAR_BOTTOM - loc, BAR_FILL_U, 0.0F, BAR_FILL_W, loc, BAR_FILL_W, loc, TEX_SIZE, TEX_SIZE,
                    ARGB.color(Math.round(BAR_FILL_ALPHA * 255.0F), energyColor(mc)));
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, BAR_FRAME_X, BAR_FRAME_Y, BAR_FRAME_U, 0.0F, BAR_FRAME_W, BAR_FRAME_H, BAR_FRAME_W, BAR_FRAME_H, TEX_SIZE, TEX_SIZE);
        if (showAmount) {
            graphics.pose().pushMatrix();
            graphics.pose().rotate((float) Math.toRadians(-90.0));
            graphics.text(mc.font, charge + " / " + max, AMOUNT_TEXT_X, AMOUNT_TEXT_Y, WHITE, false);
            graphics.pose().popMatrix();
        }
        graphics.pose().popMatrix();
    }

    private static int energyColor(Minecraft mc) {
        if (mc.level == null) {
            return DEFAULT_ENERGY_COLOR;
        }
        return mc.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(TCAspects.POTENTIA).map(holder -> holder.value().color()).orElse(DEFAULT_ENERGY_COLOR);
    }
}
