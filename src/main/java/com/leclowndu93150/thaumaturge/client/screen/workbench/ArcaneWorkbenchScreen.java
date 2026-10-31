package com.leclowndu93150.thaumaturge.client.screen.workbench;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTTContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TTTooltips;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandTooltips;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchPayment;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ArcaneWorkbenchScreen extends AbstractTTContainerScreen<MenuArcaneWorkbench> {
    private static final Identifier WAND_SLOT_TEXTURE = TTIds.rl("textures/gui/workbench_wand_slot.png");
    private static final float CRYSTAL_SPACING_DEGREES = 60.0F;
    private static final float CRYSTAL_SPIN_TICKS_PER_RADIAN = 75.0F;
    private static final int WAND_SLOT_TEX_W = 38;
    private static final int WAND_SLOT_TEX_H = 34;
    private static final int WAND_SLOT_TEX_OFFSET_X = 10;
    private static final int WAND_SLOT_TEX_OFFSET_Y = 6;
    private static final int WAND_COST_Y_OFFSET = 129;
    private static final int WAND_COST_HOVER_MIN_HALF_WIDTH = 10;
    private static final int VIS_TEXT_CENTER_X = 168;
    private static final int VIS_AVAILABLE_Y = 46;
    private static final int VIS_REQUIRED_Y = 38;
    private static final float VIS_TEXT_SCALE = 0.5F;
    private static final int TEXT_HOVER_HEIGHT = 6;
    private static final int VIS_AVAILABLE_COLOR = 0xFF6E6EEE;
    private static final int VIS_SHORT_COLOR = 0xFFEE6E6E;
    private static final int VIS_REQUIRED_COLOR = 0xFFC0FFFF;
    private static final int PERCENT = 100;
    private static final int SLOT_SIZE = 16;
    private static final DecimalFormat WAND_COST_FORMAT = new DecimalFormat("#.##");

    public ArcaneWorkbenchScreen(MenuArcaneWorkbench menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TTScreenTextures.ARCANE_WORKBENCH, 190, 234);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int availableVis = menu.getCachedVis();
        IArcaneRecipe recipe = ThaumaturgeCraftingManager.findMatchingArcaneRecipe(minecraft.level, menu.getCraftingInventory().asArcaneCraftInput(), minecraft.player);
        WorkbenchPayment.Plan plan = null;
        if (recipe != null && recipe.doesPassGate(minecraft.player)) {
            plan = WorkbenchPayment.plan(recipe, menu.getCraftingInventory(), minecraft.player);
        }
        int requiredVis = plan == null ? 0 : plan.auraVis();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, WAND_SLOT_TEXTURE, x + MenuArcaneWorkbench.WAND_X - WAND_SLOT_TEX_OFFSET_X, y + MenuArcaneWorkbench.WAND_Y - WAND_SLOT_TEX_OFFSET_Y, 0.0F, 0.0F,
                WAND_SLOT_TEX_W, WAND_SLOT_TEX_H, WAND_SLOT_TEX_W, WAND_SLOT_TEX_H, WAND_SLOT_TEX_W, WAND_SLOT_TEX_H);

        if (plan != null && !plan.crystalsToConsume().isEmpty()) {
            for (AspectInstance instance : plan.crystalsToConsume().entries()) {
                int color = instance.aspect().value().color();
                int index = MenuArcaneWorkbench.PRIMAL_ORDER.indexOf(instance.aspect().getKey());
                int slotX = x + MenuArcaneWorkbench.CRYSTAL_X[index];
                int slotY = y + MenuArcaneWorkbench.CRYSTAL_Y[index];
                graphics.pose().pushMatrix();
                graphics.pose().translate(slotX + 7.5F, slotY + 8F);
                graphics.pose().rotate(index * CRYSTAL_SPACING_DEGREES * Mth.DEG_TO_RAD + minecraft.getCameraEntity().tickCount / CRYSTAL_SPIN_TICKS_PER_RADIAN);
                graphics.pose().scale(0.5f);
                graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.ARCANE_WORKBENCH, -32, -32, 192, 0, 64, 64, 256, 256, ARGB.color(128, color));
                graphics.pose().popMatrix();
                if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                    Component hint = TTTooltips.workbenchCrystalNeeded(WandEconomy.CRYSTAL_SUBSTITUTE_VIS);
                    ItemStack crystal = menu.getCraftingInventory().getItem(MenuArcaneWorkbench.CRYSTAL_SLOT_START + index);
                    if (crystal.isEmpty()) {
                        graphics.setTooltipForNextFrame(font, hint, mouseX, mouseY);
                    } else {
                        graphics.setTooltipForNextFrame(font, List.of(crystal.getHoverName(), hint), Optional.empty(), mouseX, mouseY);
                    }
                }
            }
        }

        boolean lacking = requiredVis > availableVis;
        Component available = Component.translatable("gui.thaumaturge.arcane_workbench.vis_available", availableVis);
        if (smallCenteredText(graphics, available, x + VIS_TEXT_CENTER_X, y + VIS_AVAILABLE_Y, lacking ? VIS_SHORT_COLOR : VIS_AVAILABLE_COLOR, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font, TTTooltips.workbenchAuraVis(lacking), Optional.empty(), mouseX, mouseY);
        }

        if (plan != null && requiredVis > 0) {
            int baseVis = recipe.visCost();
            Component required;
            List<Component> requiredTooltip;
            if (requiredVis < baseVis) {
                int discountPercentage = Math.round(PERCENT - requiredVis * (float) PERCENT / baseVis);
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis_discount", requiredVis, discountPercentage);
                requiredTooltip = TTTooltips.workbenchVisCostDiscounted(discountPercentage);
            } else if (!plan.crystalsToConsume().isEmpty()) {
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis_crude", requiredVis);
                requiredTooltip = TTTooltips.workbenchVisCostUnfocused(Math.round((WandEconomy.CRAFT_AURA_SURCHARGE - 1.0F) * PERCENT));
            } else {
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis", requiredVis);
                requiredTooltip = TTTooltips.workbenchVisCost();
            }
            if (smallCenteredText(graphics, required, x + VIS_TEXT_CENTER_X, y + VIS_REQUIRED_Y, VIS_REQUIRED_COLOR, mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(font, requiredTooltip, Optional.empty(), mouseX, mouseY);
            }
        }

        if (plan != null && !plan.wandCentivis().isEmpty()) {
            MutableComponent amounts = null;
            for (Map.Entry<ResourceKey<IAspect>, Integer> entry : plan.wandCentivis().entrySet()) {
                float cost = entry.getValue() / (float) WandEconomy.CENTIVIS_PER_VIS;
                Component chunk = Component.literal(WAND_COST_FORMAT.format(cost)).withStyle(WandTooltips.primalColor(minecraft.level.registryAccess(), entry.getKey()));
                if (amounts == null) {
                    amounts = Component.empty().append(chunk);
                } else {
                    amounts.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY)).append(chunk);
                }
            }
            int lineCenterX = x + MenuArcaneWorkbench.WAND_X + 8;
            int lineY = y + WAND_COST_Y_OFFSET;
            graphics.pose().pushMatrix();
            graphics.pose().translate(lineCenterX, lineY);
            graphics.pose().scale(0.5f);
            int amountsWidth = font.width(amounts) / 2;
            graphics.text(font, amounts, -amountsWidth, 0, 0xFFFFFFFF, false);
            graphics.pose().popMatrix();
            int hoverHalfWidth = Math.max(amountsWidth / 2, WAND_COST_HOVER_MIN_HALF_WIDTH);
            if (overText(mouseX, mouseY, lineCenterX, lineY, hoverHalfWidth)) {
                graphics.setTooltipForNextFrame(font, TTTooltips.workbenchWandVis(WandEconomy.CRYSTAL_SUBSTITUTE_VIS), Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private boolean smallCenteredText(GuiGraphicsExtractor graphics, Component text, int centerX, int top, int color, int mouseX, int mouseY) {
        int width = font.width(text);
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, top);
        graphics.pose().scale(VIS_TEXT_SCALE);
        graphics.text(font, text, -width / 2, 0, color, false);
        graphics.pose().popMatrix();
        return overText(mouseX, mouseY, centerX, top, Math.round(width * VIS_TEXT_SCALE / 2));
    }

    private static boolean overText(int mouseX, int mouseY, int centerX, int top, int halfWidth) {
        return mouseX >= centerX - halfWidth && mouseX < centerX + halfWidth && mouseY >= top - 1 && mouseY < top + TEXT_HOVER_HEIGHT;
    }
}
