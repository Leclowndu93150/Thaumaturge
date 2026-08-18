package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintSource;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapelessCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jspecify.annotations.Nullable;

public final class RecipeDisplayWidget {
    public static final int PANEL_SIZE = 104;
    public static final int CENTER_OFFSET = 52;

    private static final int WORKBENCH_PANEL_U = 60;
    private static final int WORKBENCH_PANEL_V = 15;
    private static final int WORKBENCH_PANEL_W = 51;
    private static final int WORKBENCH_PANEL_H = 52;
    private static final int WORKBENCH_PANEL_OFFSET_X = -26;
    private static final int WORKBENCH_PANEL_OFFSET_Y = -26;

    private static final int ARCANE_PANEL_U = 112;
    private static final int ARCANE_PANEL_V = 15;
    private static final int ARCANE_PANEL_W = 52;
    private static final int ARCANE_PANEL_H = 52;
    private static final int ARCANE_PANEL_OFFSET_X = -26;
    private static final int ARCANE_PANEL_OFFSET_Y = -26;

    private static final int SLOT_FRAME_U = 20;
    private static final int SLOT_FRAME_V = 3;
    private static final int SLOT_FRAME_W = 16;
    private static final int SLOT_FRAME_H = 16;
    private static final int SLOT_FRAME_OFFSET_X = -8;
    private static final int SLOT_FRAME_OFFSET_Y = -46;

    private static final int VIS_COST_U = 68;
    private static final int VIS_COST_V = 76;
    private static final int VIS_COST_W = 12;
    private static final int VIS_COST_H = 12;
    private static final int VIS_COST_OFFSET_X = -6;
    private static final int VIS_COST_OFFSET_Y = 40;

    private static final float PANEL_SCALE = 2.0F;

    private static final int OUTPUT_OFFSET_X = -8;
    private static final int OUTPUT_OFFSET_Y = -84;

    private static final int GRID_ANCHOR_X = -40;
    private static final int GRID_ANCHOR_Y = -40;
    private static final int GRID_STRIDE = 32;
    private static final int GRID_DIM_MAX = 3;

    private static final int CRYSTAL_BASE_OFFSET_X = 4;
    private static final int CRYSTAL_STRIDE = 20;
    private static final int CRYSTAL_HALF_STRIDE = 10;
    private static final int CRYSTAL_OFFSET_Y = 59;

    private static final int LABEL_OFFSET_Y = -104;
    private static final int VIS_TEXT_OFFSET_Y = 90;

    private static final int VIS_POPUP_OFFSET_X = -15;
    private static final int VIS_POPUP_OFFSET_Y = 75;
    private static final int VIS_POPUP_W = 30;
    private static final int VIS_POPUP_H = 30;

    private static final int LABEL_COLOR = 0xFF504E50;

    private static final int VIS_OVERLAY_TINT = 0x66FFFFFF;

    private static final int ITEM_HIT_SIZE = 16;

    private static final long CYCLE_SECONDS = 1000L;

    private RecipeDisplayWidget() {}

    public static int width() {
        return PANEL_SIZE;
    }

    public static int height() {
        return PANEL_SIZE;
    }

    public static void renderCrafting(GuiGraphics graphics, int x, int y, RecipeHolder<?> holder, long gameTime) {
        int cx = x + CENTER_OFFSET;
        int cy = y + CENTER_OFFSET;
        Recipe<?> recipeValue = holder.value();
        if (recipeValue instanceof CrucibleRecipe crucible) {
            drawCruciblePage(graphics, cx, cy, crucible);
            return;
        }
        if (recipeValue instanceof IInfusionRecipe infusion) {
            drawInfusionPage(graphics, cx, cy, infusion);
            return;
        }
        if (recipeValue instanceof DustTriggerMultiblockRecipe multiblock) {
            drawConstructPage(graphics, cx, cy, multiblock);
            return;
        }
        Layout layout = collect(holder, registries());
        Font font = Minecraft.getInstance().font;
        if (layout.kind == Kind.ARCANE_SHAPED || layout.kind == Kind.ARCANE_SHAPELESS) {
            drawArcanePanel(graphics, cx, cy);
            drawVisOverlay(graphics, cx, cy);
            drawVisCostText(graphics, font, cx, cy, layout.visCost);
            drawCrystals(graphics, cx, cy, layout.crystals);
        } else {
            drawWorkbenchPanel(graphics, cx, cy);
        }
        drawSlotFrame(graphics, cx, cy);
        drawLabel(graphics, font, cx, cy, layout.kind);
        drawOutput(graphics, cx, cy, layout.output);
        drawInputs(graphics, cx, cy, layout);
    }

    public static @Nullable ItemStack hoverStackForDisplay(
            int x, int y, RecipeHolder<?> holder, long gameTime, double mouseX, double mouseY) {
        int cx = x + CENTER_OFFSET;
        int cy = y + CENTER_OFFSET;
        Recipe<?> recipeValue = holder.value();
        if (recipeValue instanceof CrucibleRecipe crucible) {
            return hoverCruciblePage(cx, cy, crucible, mouseX, mouseY);
        }
        if (recipeValue instanceof IInfusionRecipe infusion) {
            return hoverInfusionPage(cx, cy, infusion, mouseX, mouseY);
        }
        if (recipeValue instanceof DustTriggerMultiblockRecipe multiblock) {
            return hoverConstructPage(cx, cy, multiblock, mouseX, mouseY);
        }
        Layout layout = collect(holder, registries());
        ItemStack inputHover = hoverInput(cx, cy, layout, mouseX, mouseY);
        if (inputHover != null && !inputHover.isEmpty()) {
            return inputHover;
        }
        if (!layout.output.isEmpty()
                && mouseX >= cx + OUTPUT_OFFSET_X
                && mouseX < cx + OUTPUT_OFFSET_X + ITEM_HIT_SIZE
                && mouseY >= cy + OUTPUT_OFFSET_Y
                && mouseY < cy + OUTPUT_OFFSET_Y + ITEM_HIT_SIZE) {
            return layout.output;
        }
        if (layout.kind == Kind.ARCANE_SHAPED || layout.kind == Kind.ARCANE_SHAPELESS) {
            ItemStack crystalHover = hoverCrystal(cx, cy, layout.crystals, mouseX, mouseY);
            if (crystalHover != null && !crystalHover.isEmpty()) {
                return crystalHover;
            }
        }
        return null;
    }

    public static @Nullable Component hoverPopupForDisplay(
            int x, int y, RecipeHolder<?> holder, double mouseX, double mouseY) {
        int cx = x + CENTER_OFFSET;
        int cy = y + CENTER_OFFSET;
        Recipe<?> recipeValue = holder.value();
        if (recipeValue instanceof CrucibleRecipe crucible) {
            List<AspectInstance> sorted = sortedAspects(crucible.aspects());
            return hoverAspectGrid(cx + CRUCIBLE_ASPECT_X, cy + CRUCIBLE_ASPECT_Y, sorted, 3, mouseX, mouseY);
        }
        if (recipeValue instanceof IInfusionRecipe infusion) {
            List<AspectInstance> sorted = sortedAspects(infusion.aspects());
            return hoverAspectGrid(cx + INFUSION_ASPECT_X, cy + INFUSION_ASPECT_Y, sorted, 5, mouseX, mouseY);
        }
        Layout layout = collect(holder, registries());
        if (layout.kind != Kind.ARCANE_SHAPED && layout.kind != Kind.ARCANE_SHAPELESS) {
            return null;
        }
        Font font = Minecraft.getInstance().font;
        int costWidth = font.width(Integer.toString(layout.visCost));
        int popupX = cx - costWidth / 2 + VIS_POPUP_OFFSET_X;
        int popupY = cy + VIS_POPUP_OFFSET_Y;
        if (mouseX >= popupX && mouseX < popupX + VIS_POPUP_W && mouseY >= popupY && mouseY < popupY + VIS_POPUP_H) {
            return Component.translatable("wandtable.text1");
        }
        return null;
    }

    private static HolderLookup.Provider registries() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? null : mc.level.registryAccess();
    }

    private static void drawWorkbenchPanel(GuiGraphics graphics, int cx, int cy) {
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE, 1F);
        GuiBlend.blitTinted(
                graphics,
                TCScreenTextures.RESEARCH_BOOK_OVERLAY,
                WORKBENCH_PANEL_OFFSET_X,
                WORKBENCH_PANEL_OFFSET_Y,
                (float) WORKBENCH_PANEL_U,
                (float) WORKBENCH_PANEL_V,
                WORKBENCH_PANEL_W,
                WORKBENCH_PANEL_H,
                TCScreenTextures.TEX_SIZE,
                TCScreenTextures.TEX_SIZE,
                0xFFFFFFFF);
        graphics.pose().popPose();
    }

    private static void drawArcanePanel(GuiGraphics graphics, int cx, int cy) {
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE, 1F);
        GuiBlend.blitTinted(
                graphics,
                TCScreenTextures.RESEARCH_BOOK_OVERLAY,
                ARCANE_PANEL_OFFSET_X,
                ARCANE_PANEL_OFFSET_Y,
                (float) ARCANE_PANEL_U,
                (float) ARCANE_PANEL_V,
                ARCANE_PANEL_W,
                ARCANE_PANEL_H,
                TCScreenTextures.TEX_SIZE,
                TCScreenTextures.TEX_SIZE,
                0xFFFFFFFF);
        graphics.pose().popPose();
    }

    private static void drawSlotFrame(GuiGraphics graphics, int cx, int cy) {
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE, 1F);
        GuiBlend.blitTinted(
                graphics,
                TCScreenTextures.RESEARCH_BOOK_OVERLAY,
                SLOT_FRAME_OFFSET_X,
                SLOT_FRAME_OFFSET_Y,
                (float) SLOT_FRAME_U,
                (float) SLOT_FRAME_V,
                SLOT_FRAME_W,
                SLOT_FRAME_H,
                TCScreenTextures.TEX_SIZE,
                TCScreenTextures.TEX_SIZE,
                0xFFFFFFFF);
        graphics.pose().popPose();
    }

    private static void drawVisOverlay(GuiGraphics graphics, int cx, int cy) {
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE, 1F);
        GuiBlend.blitTinted(
                graphics,
                TCScreenTextures.RESEARCH_BOOK_OVERLAY,
                VIS_COST_OFFSET_X,
                VIS_COST_OFFSET_Y,
                (float) VIS_COST_U,
                (float) VIS_COST_V,
                VIS_COST_W,
                VIS_COST_H,
                TCScreenTextures.TEX_SIZE,
                TCScreenTextures.TEX_SIZE,
                VIS_OVERLAY_TINT);
        graphics.pose().popPose();
    }

    private static void drawVisCostText(GuiGraphics graphics, Font font, int cx, int cy, int visCost) {
        String text = Integer.toString(visCost);
        int offset = font.width(text);
        graphics.drawString(font, Component.literal(text), cx - offset / 2, cy + VIS_TEXT_OFFSET_Y, LABEL_COLOR, false);
    }

    private static void drawLabel(GuiGraphics graphics, Font font, int cx, int cy, Kind kind) {
        String key = labelKey(kind);
        if (key == null) return;
        Component text = Component.translatable(key);
        int offset = font.width(text);
        graphics.drawString(font, text, cx - offset / 2, cy + LABEL_OFFSET_Y, LABEL_COLOR, false);
    }

    private static @Nullable String labelKey(Kind kind) {
        return switch (kind) {
            case WORKBENCH_SHAPED -> "recipe.type.workbench";
            case WORKBENCH_SHAPELESS -> "recipe.type.workbenchshapeless";
            case ARCANE_SHAPED -> "recipe.type.arcane";
            case ARCANE_SHAPELESS -> "recipe.type.arcane.shapeless";
            case UNKNOWN -> null;
        };
    }

    private static void drawOutput(GuiGraphics graphics, int cx, int cy, ItemStack output) {
        if (output.isEmpty()) return;
        graphics.renderItem(output, cx + OUTPUT_OFFSET_X, cy + OUTPUT_OFFSET_Y);
    }

    private static void drawInputs(GuiGraphics graphics, int cx, int cy, Layout layout) {
        for (Slot slot : layout.slots) {
            ItemStack stack = pickRotating(slot.cycle, slot.counter);
            if (!stack.isEmpty()) {
                graphics.renderItem(
                        stack,
                        cx + GRID_ANCHOR_X + slot.col * GRID_STRIDE,
                        cy + GRID_ANCHOR_Y + slot.row * GRID_STRIDE);
            }
        }
    }

    private static void drawCrystals(GuiGraphics graphics, int cx, int cy, List<ItemStack> crystals) {
        if (crystals.isEmpty()) return;
        int sz = crystals.size();
        for (int a = 0; a < sz; a++) {
            ItemStack stack = crystals.get(a);
            if (stack.isEmpty()) continue;
            graphics.renderItem(
                    stack,
                    cx + CRYSTAL_BASE_OFFSET_X - sz * CRYSTAL_HALF_STRIDE + a * CRYSTAL_STRIDE,
                    cy + CRYSTAL_OFFSET_Y);
        }
    }

    private static @Nullable ItemStack hoverInput(int cx, int cy, Layout layout, double mouseX, double mouseY) {
        for (Slot slot : layout.slots) {
            int slotX = cx + GRID_ANCHOR_X + slot.col * GRID_STRIDE;
            int slotY = cy + GRID_ANCHOR_Y + slot.row * GRID_STRIDE;
            if (mouseX < slotX || mouseX >= slotX + ITEM_HIT_SIZE) continue;
            if (mouseY < slotY || mouseY >= slotY + ITEM_HIT_SIZE) continue;
            ItemStack stack = pickRotating(slot.cycle, slot.counter);
            if (!stack.isEmpty()) return stack;
        }
        return null;
    }

    private static @Nullable ItemStack hoverCrystal(
            int cx, int cy, List<ItemStack> crystals, double mouseX, double mouseY) {
        if (crystals.isEmpty()) return null;
        int sz = crystals.size();
        for (int a = 0; a < sz; a++) {
            ItemStack stack = crystals.get(a);
            if (stack.isEmpty()) continue;
            int slotX = cx + CRYSTAL_BASE_OFFSET_X - sz * CRYSTAL_HALF_STRIDE + a * CRYSTAL_STRIDE;
            int slotY = cy + CRYSTAL_OFFSET_Y;
            if (mouseX < slotX || mouseX >= slotX + ITEM_HIT_SIZE) continue;
            if (mouseY < slotY || mouseY >= slotY + ITEM_HIT_SIZE) continue;
            return stack;
        }
        return null;
    }

    private static Layout collect(RecipeHolder<?> holder, HolderLookup.Provider reg) {
        Recipe<?> recipe = holder.value();
        if (recipe instanceof ArcaneShapedCraftingRecipe arcane) {
            return collectArcaneShaped(arcane, reg);
        }
        if (recipe instanceof ArcaneShapelessCraftingRecipe arcane) {
            return collectArcaneShapeless(arcane, reg);
        }
        if (recipe instanceof ShapedRecipe shaped) {
            return collectShaped(shaped, reg);
        }
        if (recipe instanceof ShapelessRecipe shapeless) {
            return collectShapeless(shapeless, reg);
        }
        return new Layout(Kind.UNKNOWN, new ArrayList<>(), resultOf(recipe, reg), 0, List.of());
    }

    public static ItemStack resultOf(Recipe<?> recipe, HolderLookup.Provider reg) {
        if (reg == null) {
            return ItemStack.EMPTY;
        }
        try {
            ItemStack result = recipe.getResultItem(reg);
            return result == null ? ItemStack.EMPTY : result;
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static List<ItemStack> cycle(Ingredient ingredient) {
        if (ingredient == null || ingredient.hasNoItems()) {
            return List.of();
        }
        return resolveCycle(List.of(ingredient.getItems()));
    }

    private static List<ItemStack> resolveCycle(List<ItemStack> cycle) {
        boolean bareCrystal = false;
        for (ItemStack stack : cycle) {
            if (isBareCrystal(stack)) {
                bareCrystal = true;
                break;
            }
        }
        if (!bareCrystal) {
            return cycle;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return cycle;
        }
        List<ItemStack> expanded = new ArrayList<>();
        for (ItemStack stack : cycle) {
            if (isBareCrystal(stack)) {
                expanded.addAll(EssentiaCrystalFactory.discoveredCrystals(player));
            } else {
                expanded.add(stack);
            }
        }
        return expanded.isEmpty() ? cycle : expanded;
    }

    private static boolean isBareCrystal(ItemStack stack) {
        return stack.is(TCItems.ESSENTIA_CRYSTAL.get()) && stack.get(TCDataComponents.CRYSTAL_ASPECT.get()) == null;
    }

    private static List<ItemStack> crystals(IArcaneRecipe arcane) {
        List<ItemStack> list = new ArrayList<>();
        for (AspectInstance entry : arcane.getCrystals().entries()) {
            ItemStack stack = EssentiaCrystalFactory.of(entry.aspect(), entry.amount());
            if (!stack.isEmpty()) {
                list.add(stack);
            }
        }
        return list;
    }

    private static Layout collectShaped(ShapedRecipe shaped, HolderLookup.Provider reg) {
        int rw = shaped.getWidth();
        int rh = shaped.getHeight();
        NonNullList<Ingredient> ingredients = shaped.getIngredients();
        List<Slot> slots = gridSlots(rw, rh, ingredients);
        return new Layout(Kind.WORKBENCH_SHAPED, slots, resultOf(shaped, reg), 0, List.of());
    }

    private static Layout collectShapeless(ShapelessRecipe shapeless, HolderLookup.Provider reg) {
        List<Slot> slots = linearSlots(shapeless.getIngredients());
        return new Layout(Kind.WORKBENCH_SHAPELESS, slots, resultOf(shapeless, reg), 0, List.of());
    }

    private static Layout collectArcaneShaped(ArcaneShapedCraftingRecipe arcane, HolderLookup.Provider reg) {
        int rw = arcane.getWidth();
        int rh = arcane.getHeight();
        List<Slot> slots = gridSlots(rw, rh, arcane.getIngredients());
        return new Layout(Kind.ARCANE_SHAPED, slots, resultOf(arcane, reg), arcane.getBaseVis(), crystals(arcane));
    }

    private static Layout collectArcaneShapeless(ArcaneShapelessCraftingRecipe arcane, HolderLookup.Provider reg) {
        List<Slot> slots = linearSlots(arcane.ingredients());
        return new Layout(Kind.ARCANE_SHAPELESS, slots, resultOf(arcane, reg), arcane.getBaseVis(), crystals(arcane));
    }

    private static List<Slot> gridSlots(int rw, int rh, List<Ingredient> ingredients) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < rw && i < GRID_DIM_MAX; i++) {
            for (int j = 0; j < rh && j < GRID_DIM_MAX; j++) {
                int index = i + j * rw;
                if (index >= ingredients.size()) continue;
                List<ItemStack> c = cycle(ingredients.get(index));
                if (c.isEmpty()) continue;
                slots.add(new Slot(i, j, index, c));
            }
        }
        return slots;
    }

    private static List<Slot> linearSlots(List<Ingredient> ingredients) {
        List<Slot> slots = new ArrayList<>();
        int cap = Math.min(ingredients.size(), 9);
        for (int i = 0; i < cap; i++) {
            List<ItemStack> c = cycle(ingredients.get(i));
            if (c.isEmpty()) continue;
            slots.add(new Slot(i % GRID_DIM_MAX, i / GRID_DIM_MAX, i, c));
        }
        return slots;
    }

    private static final int CRUCIBLE_HEADER_Y = -29;
    private static final int CRUCIBLE_BODY_Y = -12;
    private static final int CRUCIBLE_DRIP_X = -25;
    private static final int CRUCIBLE_DRIP_Y = -26;
    private static final int CRUCIBLE_RESULT_X = -8;
    private static final int CRUCIBLE_RESULT_Y = -50;
    private static final int CRUCIBLE_CATALYST_X = -64;
    private static final int CRUCIBLE_CATALYST_Y = -56;
    private static final int CRUCIBLE_ASPECT_X = -28;
    private static final int CRUCIBLE_ASPECT_Y = 8;

    private static final int INFUSION_PANEL_SHIFT_Y = 20;
    private static final int INFUSION_HEADER_Y = -56;
    private static final int INFUSION_BODY_Y = -36;
    private static final int INFUSION_RESULT_Y = -85;
    private static final int INFUSION_CATALYST_Y = -16;
    private static final int INFUSION_RING_CENTER_Y = -8;
    private static final int INFUSION_RING_RADIUS = 40;
    private static final int INFUSION_ASPECT_X = -48;
    private static final int INFUSION_ASPECT_Y = 50;
    private static final int INFUSION_INSTABILITY_Y = 94;
    private static final int INFUSION_INSTABILITY_MAX = 5;

    private static final int CONSTRUCT_INGREDIENT_X = -85;
    private static final int CONSTRUCT_INGREDIENT_STRIDE = 17;
    private static final int CONSTRUCT_INGREDIENT_Y = 90;

    private static final int PREVIEW_HALF_WIDTH = 70;
    private static final int PREVIEW_TOP = -60;
    private static final int PREVIEW_BOTTOM = 84;
    private static final int PREVIEW_CELL_STRIDE = 16;
    private static final int PREVIEW_CELL_HALF_STRIDE = 8;
    private static final int PREVIEW_LAYER_STRIDE = 24;

    private static final int ASPECT_CELL = 20;
    private static final int ASPECT_HALF_CELL = 10;

    private static void drawCruciblePage(GuiGraphics graphics, int cx, int cy, CrucibleRecipe display) {
        Font font = Minecraft.getInstance().font;
        drawKindLabel(graphics, font, cx, cy, "recipe.type.crucible");
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE, 1F);
        blitOverlay(graphics, -28, CRUCIBLE_HEADER_Y, 0, 3, 56, 17);
        blitOverlay(graphics, -28, CRUCIBLE_BODY_Y, 0, 20, 56, 48);
        blitOverlay(graphics, CRUCIBLE_DRIP_X, CRUCIBLE_DRIP_Y, 100, 84, 11, 13);
        graphics.pose().popPose();
        drawAspectGrid(graphics, font, cx + CRUCIBLE_ASPECT_X, cy + CRUCIBLE_ASPECT_Y, display.aspects(), 3);
        ItemStack result = resultOf(display, registries());
        if (!result.isEmpty()) {
            graphics.renderItem(result, cx + CRUCIBLE_RESULT_X, cy + CRUCIBLE_RESULT_Y);
        }
        ItemStack catalyst = pickRotating(cycle(display.catalyst()), 0);
        if (!catalyst.isEmpty()) {
            graphics.renderItem(catalyst, cx + CRUCIBLE_CATALYST_X, cy + CRUCIBLE_CATALYST_Y);
        }
    }

    private static void drawInfusionPage(GuiGraphics graphics, int cx, int cy, IInfusionRecipe display) {
        Font font = Minecraft.getInstance().font;
        drawKindLabel(graphics, font, cx, cy, "recipe.type.infusion");
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy + INFUSION_PANEL_SHIFT_Y, 0);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE, 1F);
        blitOverlay(graphics, -28, INFUSION_HEADER_Y, 0, 3, 56, 17);
        blitOverlay(graphics, -28, INFUSION_BODY_Y, 200, 77, 56, 44);
        graphics.pose().popPose();
        drawAspectGrid(graphics, font, cx + INFUSION_ASPECT_X, cy + INFUSION_ASPECT_Y, display.aspects(), 5);
        ItemStack result = display.resultItem();
        if (!result.isEmpty()) {
            graphics.renderItem(result, cx + CRUCIBLE_RESULT_X, cy + INFUSION_RESULT_Y);
        }
        ItemStack catalyst = pickRotating(cycle(display.catalyst()), 0);
        if (!catalyst.isEmpty()) {
            graphics.renderItem(catalyst, cx + CRUCIBLE_RESULT_X, cy + INFUSION_CATALYST_Y);
        }
        List<Ingredient> components = display.components();
        for (int a = 0; a < components.size(); a++) {
            ItemStack stack = pickRotating(cycle(components.get(a)), a + 1);
            if (!stack.isEmpty()) {
                int[] offset = infusionRingOffset(a, components.size());
                graphics.renderItem(stack, cx + offset[0], cy + INFUSION_RING_CENTER_Y + offset[1]);
            }
        }
        int inst = Math.min(INFUSION_INSTABILITY_MAX, display.instability() / 2);
        Component text = Component.translatable("tc.inst").append(Component.translatable("tc.inst." + inst));
        int offset = font.width(text);
        graphics.drawString(font, text, cx - offset / 2, cy + INFUSION_INSTABILITY_Y, LABEL_COLOR, false);
    }

    private static void drawConstructPage(GuiGraphics graphics, int cx, int cy, DustTriggerMultiblockRecipe display) {
        Font font = Minecraft.getInstance().font;
        drawKindLabel(graphics, font, cx, cy, "recipe.type.construct");
        drawSlotFrame(graphics, cx, cy);
        ItemStack result = display.result();
        if (!result.isEmpty()) {
            graphics.renderItem(result, cx + OUTPUT_OFFSET_X, cy + OUTPUT_OFFSET_Y);
        }
        drawBlueprintPreview(graphics, cx, cy, display.blueprintId());
        List<ItemStack> ingredients = blueprintIngredients(display.blueprintId());
        for (int a = 0; a < ingredients.size(); a++) {
            int ix = cx + CONSTRUCT_INGREDIENT_X + a * CONSTRUCT_INGREDIENT_STRIDE;
            graphics.renderItem(ingredients.get(a), ix, cy + CONSTRUCT_INGREDIENT_Y);
            graphics.renderItemDecorations(font, ingredients.get(a), ix, cy + CONSTRUCT_INGREDIENT_Y);
        }
    }

    private static void drawBlueprintPreview(GuiGraphics graphics, int cx, int cy, ResourceLocation blueprintId) {
        Blueprint blueprint = lookupBlueprint(blueprintId);
        if (blueprint == null) {
            return;
        }
        int dx = blueprint.xSize();
        int dy = blueprint.ySize();
        int dz = blueprint.zSize();
        int minPy = -(dx - 1) * PREVIEW_CELL_HALF_STRIDE;
        int maxPy = (dz - 1) * PREVIEW_CELL_HALF_STRIDE + (dy - 1) * PREVIEW_LAYER_STRIDE;
        int unscaledWidth = (dx + dz - 1) * PREVIEW_CELL_STRIDE + PREVIEW_CELL_STRIDE;
        int unscaledHeight = maxPy - minPy + PREVIEW_CELL_STRIDE;
        float scale = Math.min(
                1.0F,
                Math.min(
                        (float) (PREVIEW_HALF_WIDTH * 2) / unscaledWidth,
                        (float) (PREVIEW_BOTTOM - PREVIEW_TOP) / unscaledHeight));
        float originX = cx - unscaledWidth * scale / 2.0F;
        float originY =
                cy + PREVIEW_TOP + (PREVIEW_BOTTOM - PREVIEW_TOP - unscaledHeight * scale) / 2.0F - minPy * scale;
        graphics.pose().pushPose();
        graphics.pose().translate(originX, originY, 0);
        graphics.pose().scale(scale, scale, 1F);
        List<PreviewCell> layer = new ArrayList<>();
        for (int j = 0; j < dy; j++) {
            layer.clear();
            for (int k = dz - 1; k >= 0; k--) {
                for (int i = dx - 1; i >= 0; i--) {
                    BlueprintPart part = blueprint.cell(j, i, k);
                    if (part == null || part.source().getRepresentations().isEmpty()) {
                        continue;
                    }
                    int px = i * PREVIEW_CELL_STRIDE + k * PREVIEW_CELL_STRIDE;
                    int py = -i * PREVIEW_CELL_HALF_STRIDE + k * PREVIEW_CELL_HALF_STRIDE + j * PREVIEW_LAYER_STRIDE;
                    ItemStack stack = pickRotating(part.source().getRepresentations(), j * dx * dz + k * dx + i);
                    if (!stack.isEmpty()) {
                        layer.add(new PreviewCell(px, py, stack));
                    }
                }
            }
            layer.sort(Comparator.comparingInt(PreviewCell::py));
            for (PreviewCell cell : layer) {
                graphics.renderItem(cell.stack(), cell.px(), cell.py());
            }
        }
        graphics.pose().popPose();
    }

    private record PreviewCell(int px, int py, ItemStack stack) {}

    private static void drawKindLabel(GuiGraphics graphics, Font font, int cx, int cy, String key) {
        Component text = Component.translatable(key);
        int offset = font.width(text);
        graphics.drawString(font, text, cx - offset / 2, cy + LABEL_OFFSET_Y, LABEL_COLOR, false);
    }

    private static void blitOverlay(GuiGraphics graphics, int ox, int oy, int u, int v, int w, int h) {
        GuiBlend.blitTinted(
                graphics,
                TCScreenTextures.RESEARCH_BOOK_OVERLAY,
                ox,
                oy,
                (float) u,
                (float) v,
                w,
                h,
                TCScreenTextures.TEX_SIZE,
                TCScreenTextures.TEX_SIZE,
                0xFFFFFFFF);
    }

    private static List<AspectInstance> sortedAspects(AspectList aspects) {
        return aspects.entries().stream()
                .sorted(Comparator.comparing(e -> e.aspect().getKey().location().toString()))
                .toList();
    }

    private static void drawAspectGrid(
            GuiGraphics graphics, Font font, int sx, int sy, AspectList aspects, int perRow) {
        List<AspectInstance> sorted = sortedAspects(aspects);
        int rows = (sorted.size() - 1) / perRow;
        int startY = sy - ASPECT_HALF_CELL * rows;
        int total = 0;
        for (AspectInstance instance : sorted) {
            int[] pos = aspectCell(sx, startY, total, sorted.size(), perRow, rows);
            AspectTagRenderer.render(graphics, font, pos[0], pos[1], instance.aspect(), instance.amount());
            total++;
        }
    }

    private static int[] aspectCell(int sx, int sy, int index, int count, int perRow, int rows) {
        int shift = (perRow - count % perRow) * ASPECT_HALF_CELL;
        int m = index / perRow >= rows && (rows > 1 || count < perRow) ? 1 : 0;
        return new int[] {sx + index % perRow * ASPECT_CELL + shift * m, sy + index / perRow * ASPECT_CELL};
    }

    private static int[] infusionRingOffset(int index, int count) {
        float pieSlice = 360.0F / count;
        float rot = -90.0F + pieSlice * index;
        int xx = (int) (Mth.cos(rot / 180.0F * (float) Math.PI) * INFUSION_RING_RADIUS) - 8;
        int yy = (int) (Mth.sin(rot / 180.0F * (float) Math.PI) * INFUSION_RING_RADIUS) - 8;
        return new int[] {xx, yy};
    }

    private static @Nullable Blueprint lookupBlueprint(ResourceLocation blueprintId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        Registry<Blueprint> registry =
                mc.level.registryAccess().registry(Blueprint.REGISTRY_KEY).orElse(null);
        if (registry == null) {
            return null;
        }
        return registry.getHolder(ResourceKey.create(Blueprint.REGISTRY_KEY, blueprintId))
                .map(Holder::value)
                .orElse(null);
    }

    private static List<ItemStack> blueprintIngredients(ResourceLocation blueprintId) {
        Blueprint blueprint = lookupBlueprint(blueprintId);
        if (blueprint == null) {
            return List.of();
        }
        Map<BlueprintSource, Integer> counts = new LinkedHashMap<>();
        for (int y = 0; y < blueprint.ySize(); y++) {
            for (int x = 0; x < blueprint.xSize(); x++) {
                for (int z = 0; z < blueprint.zSize(); z++) {
                    BlueprintPart part = blueprint.cell(y, x, z);
                    if (part != null && !part.source().getRepresentations().isEmpty()) {
                        counts.merge(part.source(), 1, Integer::sum);
                    }
                }
            }
        }
        List<ItemStack> out = new ArrayList<>();
        counts.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<BlueprintSource, Integer> e) -> e.getValue())
                        .reversed())
                .forEach(e -> {
                    ItemStack stack = e.getKey().getRepresentations().get(0).copy();
                    stack.setCount(e.getValue());
                    out.add(stack);
                });
        return out;
    }

    private static @Nullable ItemStack hoverCruciblePage(
            int cx, int cy, CrucibleRecipe display, double mouseX, double mouseY) {
        ItemStack result = resultOf(display, registries());
        if (!result.isEmpty() && hitItem(cx + CRUCIBLE_RESULT_X, cy + CRUCIBLE_RESULT_Y, mouseX, mouseY)) {
            return result;
        }
        ItemStack catalyst = pickRotating(cycle(display.catalyst()), 0);
        if (!catalyst.isEmpty() && hitItem(cx + CRUCIBLE_CATALYST_X, cy + CRUCIBLE_CATALYST_Y, mouseX, mouseY)) {
            return catalyst;
        }
        return null;
    }

    private static @Nullable ItemStack hoverInfusionPage(
            int cx, int cy, IInfusionRecipe display, double mouseX, double mouseY) {
        ItemStack result = display.resultItem();
        if (!result.isEmpty() && hitItem(cx + CRUCIBLE_RESULT_X, cy + INFUSION_RESULT_Y, mouseX, mouseY)) {
            return result;
        }
        ItemStack catalyst = pickRotating(cycle(display.catalyst()), 0);
        if (!catalyst.isEmpty() && hitItem(cx + CRUCIBLE_RESULT_X, cy + INFUSION_CATALYST_Y, mouseX, mouseY)) {
            return catalyst;
        }
        List<Ingredient> components = display.components();
        for (int a = 0; a < components.size(); a++) {
            int[] offset = infusionRingOffset(a, components.size());
            if (hitItem(cx + offset[0], cy + INFUSION_RING_CENTER_Y + offset[1], mouseX, mouseY)) {
                ItemStack stack = pickRotating(cycle(components.get(a)), a + 1);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return null;
    }

    private static @Nullable ItemStack hoverConstructPage(
            int cx, int cy, DustTriggerMultiblockRecipe display, double mouseX, double mouseY) {
        ItemStack result = display.result();
        if (!result.isEmpty() && hitItem(cx + OUTPUT_OFFSET_X, cy + OUTPUT_OFFSET_Y, mouseX, mouseY)) {
            return result;
        }
        List<ItemStack> ingredients = blueprintIngredients(display.blueprintId());
        for (int a = 0; a < ingredients.size(); a++) {
            if (hitItem(
                    cx + CONSTRUCT_INGREDIENT_X + a * CONSTRUCT_INGREDIENT_STRIDE,
                    cy + CONSTRUCT_INGREDIENT_Y,
                    mouseX,
                    mouseY)) {
                return ingredients.get(a);
            }
        }
        return null;
    }

    private static boolean hitItem(int x, int y, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + ITEM_HIT_SIZE && mouseY >= y && mouseY < y + ITEM_HIT_SIZE;
    }

    private static @Nullable Component hoverAspectGrid(
            int sx, int sy, List<AspectInstance> sorted, int perRow, double mouseX, double mouseY) {
        int rows = (sorted.size() - 1) / perRow;
        int startY = sy - ASPECT_HALF_CELL * rows;
        for (int index = 0; index < sorted.size(); index++) {
            int[] pos = aspectCell(sx, startY, index, sorted.size(), perRow, rows);
            if (hitItem(pos[0], pos[1], mouseX, mouseY)) {
                AspectInstance instance = sorted.get(index);
                return AspectComponents.name(instance.aspect())
                        .append("\n")
                        .append(AspectComponents.description(instance.aspect()));
            }
        }
        return null;
    }

    private static ItemStack pickRotating(List<ItemStack> stacks, int counter) {
        if (stacks.isEmpty()) return ItemStack.EMPTY;
        long wall = System.currentTimeMillis() / CYCLE_SECONDS;
        int index = (int) Math.floorMod((long) counter + wall, (long) stacks.size());
        return stacks.get(index);
    }

    private enum Kind {
        WORKBENCH_SHAPED,
        WORKBENCH_SHAPELESS,
        ARCANE_SHAPED,
        ARCANE_SHAPELESS,
        UNKNOWN
    }

    private record Slot(int col, int row, int counter, List<ItemStack> cycle) {}

    private record Layout(Kind kind, List<Slot> slots, ItemStack output, int visCost, List<ItemStack> crystals) {}
}
