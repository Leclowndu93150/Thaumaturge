package com.leclowndu93150.thaumaturge.client.screen.workbench;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTTContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TTTooltips;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandTooltips;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.SlotCrystalEssentia;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchPayment;
import com.mojang.math.Axis;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ArcaneWorkbenchScreen extends AbstractTTContainerScreen<MenuArcaneWorkbench> {
    private static final ResourceLocation WAND_SLOT_TEXTURE = TTIds.rl("textures/gui/workbench_wand_slot.png");
    private static final int WAND_SLOT_TEX_W = 38;
    private static final int WAND_SLOT_TEX_H = 34;
    private static final int WAND_SLOT_TEX_OFFSET_X = 10;
    private static final int WAND_SLOT_TEX_OFFSET_Y = 6;
    private static final int WAND_COST_Y_OFFSET = 129;
    private static final int TEXT_HOVER_HEIGHT = 6;
    private static final int WAND_COST_HOVER_MIN_HALF_WIDTH = 10;
    private static final int VIS_TEXT_CENTER_X = 168;
    private static final int VIS_AVAILABLE_Y = 46;
    private static final int VIS_COST_Y = 38;
    private static final float VIS_TEXT_SCALE = 0.5F;
    private static final int PERCENT = 100;
    private static final int SLOT_SIZE = 16;
    private static final DecimalFormat WAND_COST_FORMAT = new DecimalFormat("#.##");

    private List<Component> hoveredTooltip;

    public ArcaneWorkbenchScreen(MenuArcaneWorkbench menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TTScreenTextures.ARCANE_WORKBENCH, 190, 234);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int xm, int ym) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        hoveredTooltip = null;
        super.render(graphics, mouseX, mouseY, partialTick);
        if (hoveredTooltip != null) {
            graphics.renderComponentTooltip(font, hoveredTooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderSlotTooltip(GuiGraphics graphics, Slot slot, int mouseX, int mouseY) {
        if (slot instanceof SlotCrystalEssentia && hoveredTooltip != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(slot.getItem().getHoverName());
            tooltip.addAll(hoveredTooltip);
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            hoveredTooltip = null;
        } else if (slot instanceof SlotCrystalEssentia) {
            graphics.renderTooltip(font, slot.getItem().getHoverName(), mouseX, mouseY);
        } else {
            super.renderSlotTooltip(graphics, slot, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int availableVis = menu.getCachedVis();
        IArcaneRecipe recipe = ThaumaturgeCraftingManager.findMatchingArcaneRecipe(
                minecraft.level, menu.getCraftingInventory().asArcaneCraftInput(), minecraft.player);
        WorkbenchPayment.Plan plan = null;
        if (recipe != null && recipe.doesPassGate(minecraft.player)) {
            plan = WorkbenchPayment.plan(recipe, menu.getCraftingInventory(), minecraft.player);
        }
        int requiredVis = plan == null ? 0 : plan.auraVis();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.blit(
                WAND_SLOT_TEXTURE,
                x + MenuArcaneWorkbench.WAND_X - WAND_SLOT_TEX_OFFSET_X,
                y + MenuArcaneWorkbench.WAND_Y - WAND_SLOT_TEX_OFFSET_Y,
                0.0F,
                0.0F,
                WAND_SLOT_TEX_W,
                WAND_SLOT_TEX_H,
                WAND_SLOT_TEX_W,
                WAND_SLOT_TEX_H);

        if (plan != null && !plan.crystalsToConsume().isEmpty()) {
            for (AspectInstance instance : plan.crystalsToConsume().entries()) {
                int color = instance.aspect().value().color();
                int index = MenuArcaneWorkbench.PRIMAL_ORDER.indexOf(
                        instance.aspect().getKey());
                int slotX = x + MenuArcaneWorkbench.CRYSTAL_X[index];
                int slotY = y + MenuArcaneWorkbench.CRYSTAL_Y[index];
                graphics.pose().pushPose();
                graphics.pose().translate(slotX + 7.5F, slotY + 8F, 0.0F);
                graphics.pose()
                        .mulPose(Axis.ZP.rotationDegrees(
                                index * 60 + ((float) minecraft.getCameraEntity().tickCount / 75) * Mth.RAD_TO_DEG));
                graphics.pose().scale(0.5f, 0.5f, 1.0f);
                GuiBlend.blitTinted(
                        graphics,
                        TTScreenTextures.ARCANE_WORKBENCH,
                        -32,
                        -32,
                        192,
                        0,
                        64,
                        64,
                        256,
                        256,
                        ARGB32.color(128, color));
                graphics.pose().popPose();
                if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                    hoveredTooltip = List.of(TTTooltips.workbenchCrystalNeeded(WandEconomy.CRYSTAL_SUBSTITUTE_VIS));
                }
            }
        }

        boolean insufficient = requiredVis > availableVis;
        Component available = Component.translatable("gui.thaumaturge.arcane_workbench.vis_available", availableVis);
        if (drawCenteredText(
                graphics,
                available,
                x + VIS_TEXT_CENTER_X,
                y + VIS_AVAILABLE_Y,
                insufficient ? 0xFFEE6E6E : 0xFF6E6EEE,
                mouseX,
                mouseY)) {
            hoveredTooltip = TTTooltips.workbenchVisAvailable(insufficient);
        }

        if (plan != null && requiredVis > 0) {
            int baseVis = recipe.visCost();
            Component required;
            List<Component> requiredTooltip;
            if (requiredVis < baseVis) {
                int discountPercentage = Math.round(PERCENT - requiredVis * (float) PERCENT / baseVis);
                required = Component.translatable(
                        "gui.thaumaturge.arcane_workbench.required_vis_discount", requiredVis, discountPercentage);
                requiredTooltip = TTTooltips.workbenchVisCostDiscounted(discountPercentage);
            } else if (!plan.crystalsToConsume().isEmpty()) {
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis_crude", requiredVis);
                requiredTooltip = TTTooltips.workbenchVisCostUnfocused(
                        Math.round((WandEconomy.CRAFT_AURA_SURCHARGE - 1.0F) * PERCENT));
            } else {
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis", requiredVis);
                requiredTooltip = TTTooltips.workbenchVisCost();
            }
            if (drawCenteredText(
                    graphics, required, x + VIS_TEXT_CENTER_X, y + VIS_COST_Y, 0xFFC0FFFF, mouseX, mouseY)) {
                hoveredTooltip = requiredTooltip;
            }
        }

        if (plan != null && !plan.wandCentivis().isEmpty()) {
            MutableComponent amounts = null;
            for (Map.Entry<ResourceKey<IAspect>, Integer> entry :
                    plan.wandCentivis().entrySet()) {
                float cost = entry.getValue() / (float) WandEconomy.CENTIVIS_PER_VIS;
                Component chunk = Component.literal(WAND_COST_FORMAT.format(cost))
                        .withStyle(WandTooltips.primalColor(minecraft.level.registryAccess(), entry.getKey()));
                if (amounts == null) {
                    amounts = Component.empty().append(chunk);
                } else {
                    amounts.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(chunk);
                }
            }
            int lineCenterX = x + MenuArcaneWorkbench.WAND_X + 8;
            int lineY = y + WAND_COST_Y_OFFSET;
            graphics.pose().pushPose();
            graphics.pose().translate(lineCenterX, lineY, 0.0F);
            graphics.pose().scale(0.5f, 0.5f, 1.0f);
            int amountsWidth = font.width(amounts) / 2;
            graphics.drawString(font, amounts, -amountsWidth, 0, 0xFFFFFFFF, false);
            graphics.pose().popPose();
            if (isOverText(amounts, lineCenterX, lineY, WAND_COST_HOVER_MIN_HALF_WIDTH, mouseX, mouseY)) {
                hoveredTooltip = TTTooltips.workbenchWandPayment(WandEconomy.CRYSTAL_SUBSTITUTE_VIS);
            }
        }
    }

    private boolean drawCenteredText(
            GuiGraphics graphics, Component text, int centerX, int top, int color, int mouseX, int mouseY) {
        int textWidth = font.width(text);
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, top, 0.0F);
        graphics.pose().scale(VIS_TEXT_SCALE, VIS_TEXT_SCALE, 1.0F);
        graphics.drawString(font, text, -textWidth / 2, 0, color, false);
        graphics.pose().popPose();
        return isOverText(text, centerX, top, 0, mouseX, mouseY);
    }

    private boolean isOverText(Component text, int centerX, int top, int minHalfWidth, int mouseX, int mouseY) {
        int halfWidth = Math.max(Math.round(font.width(text) * VIS_TEXT_SCALE / 2.0F), minHalfWidth);
        return mouseX >= centerX - halfWidth
                && mouseX < centerX + halfWidth
                && mouseY >= top - 1
                && mouseY < top + TEXT_HOVER_HEIGHT;
    }
}
