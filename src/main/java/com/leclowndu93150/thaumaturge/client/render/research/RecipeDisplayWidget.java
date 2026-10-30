package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.recipe.dust.MultiblockRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipeDisplay;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jspecify.annotations.Nullable;

public final class RecipeDisplayWidget {
    public static final int PANEL_SIZE = 104;
    public static final int CENTER_OFFSET = 52;

    private static final int ALL_LAYERS = -1;
    private static final long ROTATION_PERIOD_MILLIS = 72000L;
    private static final float ROTATION_MILLIS_PER_DEGREE = 200.0F;

    public record ItemHit(ItemStack stack, int x, int y) {
        public boolean contains(double mouseX, double mouseY) {
            return PanelContext.inside(mouseX, mouseY, x, y, PanelContext.ITEM_SIZE);
        }
    }

    private static final class PlainRecipePanel implements RecipePanel {
        private final RecipeDisplay display;

        PlainRecipePanel(RecipeDisplay display) {
            this.display = display;
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, PanelContext context, float rotation, int layer) {
            context.drawOutputFrame(graphics);
            hits(context).forEach(hit -> context.drawItem(graphics, hit));
        }

        @Override
        public List<ItemHit> hits(PanelContext context) {
            List<ItemHit> collected = new ArrayList<>(1);
            context.collect(collected, context.first(display.result()), PanelContext.OUTPUT_X, PanelContext.OUTPUT_Y);
            return collected;
        }

        @Override
        public @Nullable List<Component> popup(PanelContext context, double mouseX, double mouseY) {
            return null;
        }
    }

    private RecipeDisplayWidget() {}

    public static int width() {
        return PANEL_SIZE;
    }

    public static int height() {
        return PANEL_SIZE;
    }

    public static void renderCrafting(GuiGraphicsExtractor graphics, int x, int y, RecipeDisplay display, long gameTime) {
        renderCrafting(graphics, x, y, display, gameTime, automaticRotation(), ALL_LAYERS);
    }

    public static void renderCrafting(GuiGraphicsExtractor graphics, int x, int y, RecipeDisplay display, long gameTime, float rotation, int layer) {
        panelFor(display).render(graphics, PanelContext.at(x, y), rotation, layer);
    }

    public static @Nullable ItemStack hoverStackForDisplay(int x, int y, RecipeDisplay display, long gameTime, double mouseX, double mouseY) {
        ItemHit hit = hoverItemForDisplay(x, y, display, mouseX, mouseY);
        return hit == null ? null : hit.stack();
    }

    public static @Nullable ItemHit hoverItemForDisplay(int x, int y, RecipeDisplay display, double mouseX, double mouseY) {
        for (ItemHit hit : panelFor(display).hits(PanelContext.at(x, y))) {
            if (hit.contains(mouseX, mouseY)) {
                return hit;
            }
        }
        return null;
    }

    public static @Nullable List<Component> hoverPopupForDisplay(int x, int y, RecipeDisplay display, double mouseX, double mouseY) {
        return panelFor(display).popup(PanelContext.at(x, y), mouseX, mouseY);
    }

    public static float automaticRotation() {
        return (Util.getMillis() % ROTATION_PERIOD_MILLIS) / ROTATION_MILLIS_PER_DEGREE;
    }

    public static int multiblockLayerCount(RecipeDisplay display) {
        return display instanceof MultiblockRecipeDisplay multiblock ? ConstructRecipePanel.layerCount(multiblock) : 0;
    }

    public static void renderBookmarkIcon(GuiGraphicsExtractor graphics, int x, int y, RecipeDisplay display) {
        if (display instanceof MultiblockRecipeDisplay multiblock) {
            ConstructRecipePanel.renderBookmark(graphics, x, y, multiblock);
        } else {
            resultStack(display).ifPresent(stack -> graphics.item(stack, x, y));
        }
    }

    private static Optional<ItemStack> resultStack(RecipeDisplay display) {
        return Optional.ofNullable(Minecraft.getInstance().level).map(level -> display.result().resolveForFirstStack(SlotDisplayContext.fromLevel(level))).filter(stack -> !stack.isEmpty());
    }

    private static GridRecipePanel arcanePanel(ArcaneCraftingRecipeDisplay arcane) {
        GridRecipePanel.Kind kind = GridRecipePanel.Kind.ARCANE;
        if (arcane.shapeless()) {
            kind = GridRecipePanel.Kind.ARCANE_SHAPELESS;
        }
        return new GridRecipePanel(kind, arcane.width(), arcane.height(), arcane.ingredients(), arcane.result(), arcane.crystals(), arcane.visCost());
    }

    private static RecipePanel panelFor(RecipeDisplay display) {
        if (display instanceof CrucibleRecipeDisplay crucible) {
            return new CrucibleRecipePanel(crucible.catalyst(), crucible.aspects(), crucible.result());
        }
        if (display instanceof InfusionRecipeDisplay infusion) {
            return new InfusionRecipePanel(infusion.catalyst(), infusion.components(), infusion.aspects(), infusion.instability(), infusion.result());
        }
        if (display instanceof MultiblockRecipeDisplay multiblock) {
            return new ConstructRecipePanel(multiblock);
        }
        if (display instanceof ArcaneCraftingRecipeDisplay arcane) {
            return arcanePanel(arcane);
        }
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            return new GridRecipePanel(GridRecipePanel.Kind.WORKBENCH, shaped.width(), shaped.height(), shaped.ingredients(), shaped.result(), List.of(), 0);
        }
        if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            return new GridRecipePanel(GridRecipePanel.Kind.WORKBENCH_SHAPELESS, 0, 0, shapeless.ingredients(), shapeless.result(), List.of(), 0);
        }
        if (display instanceof SmithingRecipeDisplay smithing) {
            return new SmithingRecipePanel(smithing.template(), smithing.base(), smithing.addition(), smithing.result());
        }
        return new PlainRecipePanel(display);
    }
}
