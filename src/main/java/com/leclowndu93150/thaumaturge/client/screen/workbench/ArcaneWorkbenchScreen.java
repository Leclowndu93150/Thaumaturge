package com.leclowndu93150.thaumaturge.client.screen.workbench;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandTooltips;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchPayment;
import java.text.DecimalFormat;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public class ArcaneWorkbenchScreen extends AbstractTCContainerScreen<MenuArcaneWorkbench> {
    private static final Identifier WAND_SLOT_TEXTURE = TCIds.rl("textures/gui/workbench_wand_slot.png");
    private static final int WAND_SLOT_TEX_W = 38;
    private static final int WAND_SLOT_TEX_H = 34;
    private static final int WAND_SLOT_TEX_OFFSET_X = 10;
    private static final int WAND_SLOT_TEX_OFFSET_Y = 6;
    private static final int WAND_COST_Y_OFFSET = 129;
    private static final int WAND_COST_HOVER_HEIGHT = 6;
    private static final int WAND_COST_HOVER_MIN_HALF_WIDTH = 10;
    private static final DecimalFormat WAND_COST_FORMAT = new DecimalFormat("#.##");

    public ArcaneWorkbenchScreen(MenuArcaneWorkbench menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TCScreenTextures.ARCANE_WORKBENCH, 190, 234);
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
                graphics.pose().pushMatrix();
                graphics.pose().translate(x + MenuArcaneWorkbench.CRYSTAL_X[index] + 7.5F, y + MenuArcaneWorkbench.CRYSTAL_Y[index] + 8F);
                graphics.pose().rotate(index * 60 + ((float) minecraft.getCameraEntity().tickCount / 75) % 360);
                graphics.pose().scale(0.5f);
                graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.ARCANE_WORKBENCH, -32, -32, 192, 0, 64, 64, 256, 256, ARGB.color(128, color));
                graphics.pose().popMatrix();
            }
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 168, y + 46);
        graphics.pose().scale(0.5f);
        Component available = Component.translatable("gui.thaumaturge.arcane_workbench.vis_available", availableVis);
        int availableWidth = font.width(available) / 2;
        graphics.text(font, available, -availableWidth, 0, 0xFF000000 | (requiredVis > availableVis ? 15625838 : 7237358), false);
        graphics.pose().popMatrix();

        if (plan != null && requiredVis > 0) {
            int baseVis = recipe.getBaseVis();
            Component required;
            if (requiredVis < baseVis) {
                int discountPercentage = Math.round(100.0F - requiredVis * 100.0F / baseVis);
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis_discount", requiredVis, discountPercentage);
            } else if (!plan.crystalsToConsume().isEmpty()) {
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis_crude", requiredVis);
            } else {
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis", requiredVis);
            }
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + 168, y + 38);
            graphics.pose().scale(0.5f);
            int requiredWidth = font.width(required) / 2;
            graphics.text(font, required, -requiredWidth, 0, 0xFF000000 | 12648447, false);
            graphics.pose().popMatrix();
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
            if (mouseX >= lineCenterX - hoverHalfWidth && mouseX < lineCenterX + hoverHalfWidth && mouseY >= lineY - 1 && mouseY < lineY + WAND_COST_HOVER_HEIGHT) {
                graphics.setTooltipForNextFrame(font, Component.translatable("gui.thaumaturge.arcane_workbench.wand_pay.tooltip", WandEconomy.CRYSTAL_SUBSTITUTE_VIS), mouseX, mouseY);
            }
        }
    }
}
