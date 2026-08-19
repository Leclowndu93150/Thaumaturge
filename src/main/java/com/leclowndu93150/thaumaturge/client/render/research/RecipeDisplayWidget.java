package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintSource;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.pip.BlockPreviewRenderState;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.recipe.dust.MultiblockRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.mixin.client.gui.GuiGraphicsExtractorAccessor;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
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

    public static void renderCrafting(GuiGraphicsExtractor graphics, int x, int y, RecipeDisplay display, long gameTime) {
        int cx = x + CENTER_OFFSET;
        int cy = y + CENTER_OFFSET;
        ContextMap context = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        if (display instanceof CrucibleRecipeDisplay crucible) {
            drawCruciblePage(graphics, cx, cy, crucible, context);
            return;
        }
        if (display instanceof InfusionRecipeDisplay infusion) {
            drawInfusionPage(graphics, cx, cy, infusion, context);
            return;
        }
        if (display instanceof MultiblockRecipeDisplay multiblock) {
            drawConstructPage(graphics, cx, cy, multiblock, context);
            return;
        }
        Layout layout = collect(display, context);
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

    public static @Nullable ItemStack hoverStackForDisplay(int x, int y, RecipeDisplay display, long gameTime, double mouseX, double mouseY) {
        int cx = x + CENTER_OFFSET;
        int cy = y + CENTER_OFFSET;
        ContextMap context = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        if (display instanceof CrucibleRecipeDisplay crucible) {
            return hoverCruciblePage(cx, cy, crucible, context, mouseX, mouseY);
        }
        if (display instanceof InfusionRecipeDisplay infusion) {
            return hoverInfusionPage(cx, cy, infusion, context, mouseX, mouseY);
        }
        if (display instanceof MultiblockRecipeDisplay multiblock) {
            return hoverConstructPage(cx, cy, multiblock, context, mouseX, mouseY);
        }
        Layout layout = collect(display, context);
        ItemStack inputHover = hoverInput(cx, cy, layout, mouseX, mouseY);
        if (inputHover != null && !inputHover.isEmpty()) {
            return inputHover;
        }
        if (!layout.output.isEmpty() && mouseX >= cx + OUTPUT_OFFSET_X && mouseX < cx + OUTPUT_OFFSET_X + ITEM_HIT_SIZE && mouseY >= cy + OUTPUT_OFFSET_Y
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

    public static @Nullable Component hoverPopupForDisplay(int x, int y, RecipeDisplay display, double mouseX, double mouseY) {
        int cx = x + CENTER_OFFSET;
        int cy = y + CENTER_OFFSET;
        ContextMap context = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        if (display instanceof CrucibleRecipeDisplay crucible) {
            List<AspectInstance> sorted = sortedAspects(crucible.aspects());
            return hoverAspectGrid(cx + CRUCIBLE_ASPECT_X, cy + CRUCIBLE_ASPECT_Y, sorted, 3, mouseX, mouseY);
        }
        if (display instanceof InfusionRecipeDisplay infusion) {
            List<AspectInstance> sorted = sortedAspects(infusion.aspects());
            return hoverAspectGrid(cx + INFUSION_ASPECT_X, cy + INFUSION_ASPECT_Y, sorted, 5, mouseX, mouseY);
        }
        Layout layout = collect(display, context);
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

    private static void drawWorkbenchPanel(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK_OVERLAY, WORKBENCH_PANEL_OFFSET_X, WORKBENCH_PANEL_OFFSET_Y, (float) WORKBENCH_PANEL_U, (float) WORKBENCH_PANEL_V,
                WORKBENCH_PANEL_W, WORKBENCH_PANEL_H, WORKBENCH_PANEL_W, WORKBENCH_PANEL_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        graphics.pose().popMatrix();
    }

    private static void drawArcanePanel(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK_OVERLAY, ARCANE_PANEL_OFFSET_X, ARCANE_PANEL_OFFSET_Y, (float) ARCANE_PANEL_U, (float) ARCANE_PANEL_V,
                ARCANE_PANEL_W, ARCANE_PANEL_H, ARCANE_PANEL_W, ARCANE_PANEL_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        graphics.pose().popMatrix();
    }

    private static void drawSlotFrame(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK_OVERLAY, SLOT_FRAME_OFFSET_X, SLOT_FRAME_OFFSET_Y, (float) SLOT_FRAME_U, (float) SLOT_FRAME_V, SLOT_FRAME_W,
                SLOT_FRAME_H, SLOT_FRAME_W, SLOT_FRAME_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        graphics.pose().popMatrix();
    }

    private static void drawVisOverlay(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK_OVERLAY, VIS_COST_OFFSET_X, VIS_COST_OFFSET_Y, (float) VIS_COST_U, (float) VIS_COST_V, VIS_COST_W, VIS_COST_H,
                VIS_COST_W, VIS_COST_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, VIS_OVERLAY_TINT);
        graphics.pose().popMatrix();
    }

    private static void drawVisCostText(GuiGraphicsExtractor graphics, Font font, int cx, int cy, int visCost) {
        String text = Integer.toString(visCost);
        int offset = font.width(text);
        graphics.text(font, Component.literal(text), cx - offset / 2, cy + VIS_TEXT_OFFSET_Y, LABEL_COLOR, false);
    }

    private static void drawLabel(GuiGraphicsExtractor graphics, Font font, int cx, int cy, Kind kind) {
        String key = labelKey(kind);
        if (key == null)
            return;
        Component text = Component.translatable(key);
        int offset = font.width(text);
        graphics.text(font, text, cx - offset / 2, cy + LABEL_OFFSET_Y, LABEL_COLOR, false);
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

    private static void drawOutput(GuiGraphicsExtractor graphics, int cx, int cy, ItemStack output) {
        if (output.isEmpty())
            return;
        graphics.item(output, cx + OUTPUT_OFFSET_X, cy + OUTPUT_OFFSET_Y);
    }

    private static void drawInputs(GuiGraphicsExtractor graphics, int cx, int cy, Layout layout) {
        for (Slot slot : layout.slots) {
            ItemStack stack = pickRotating(slot.cycle, slot.counter);
            if (!stack.isEmpty()) {
                graphics.item(stack, cx + GRID_ANCHOR_X + slot.col * GRID_STRIDE, cy + GRID_ANCHOR_Y + slot.row * GRID_STRIDE);
            }
        }
    }

    private static void drawCrystals(GuiGraphicsExtractor graphics, int cx, int cy, List<ItemStack> crystals) {
        if (crystals.isEmpty())
            return;
        int sz = crystals.size();
        for (int a = 0; a < sz; a++) {
            ItemStack stack = crystals.get(a);
            if (stack.isEmpty())
                continue;
            graphics.item(stack, cx + CRYSTAL_BASE_OFFSET_X - sz * CRYSTAL_HALF_STRIDE + a * CRYSTAL_STRIDE, cy + CRYSTAL_OFFSET_Y);
        }
    }

    private static @Nullable ItemStack hoverInput(int cx, int cy, Layout layout, double mouseX, double mouseY) {
        for (Slot slot : layout.slots) {
            int slotX = cx + GRID_ANCHOR_X + slot.col * GRID_STRIDE;
            int slotY = cy + GRID_ANCHOR_Y + slot.row * GRID_STRIDE;
            if (mouseX < slotX || mouseX >= slotX + ITEM_HIT_SIZE)
                continue;
            if (mouseY < slotY || mouseY >= slotY + ITEM_HIT_SIZE)
                continue;
            ItemStack stack = pickRotating(slot.cycle, slot.counter);
            if (!stack.isEmpty())
                return stack;
        }
        return null;
    }

    private static @Nullable ItemStack hoverCrystal(int cx, int cy, List<ItemStack> crystals, double mouseX, double mouseY) {
        if (crystals.isEmpty())
            return null;
        int sz = crystals.size();
        for (int a = 0; a < sz; a++) {
            ItemStack stack = crystals.get(a);
            if (stack.isEmpty())
                continue;
            int slotX = cx + CRYSTAL_BASE_OFFSET_X - sz * CRYSTAL_HALF_STRIDE + a * CRYSTAL_STRIDE;
            int slotY = cy + CRYSTAL_OFFSET_Y;
            if (mouseX < slotX || mouseX >= slotX + ITEM_HIT_SIZE)
                continue;
            if (mouseY < slotY || mouseY >= slotY + ITEM_HIT_SIZE)
                continue;
            return stack;
        }
        return null;
    }

    private static Layout collect(RecipeDisplay display, ContextMap context) {
        if (display instanceof ArcaneCraftingRecipeDisplay arcane) {
            return collectArcane(arcane, context);
        }
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            return collectShaped(shaped, context);
        }
        if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            return collectShapeless(shapeless, context);
        }
        ItemStack result = ItemStack.EMPTY;
        if (display.result() instanceof SlotDisplay slot) {
            result = slot.resolveForFirstStack(context);
        }
        return new Layout(Kind.UNKNOWN, new ArrayList<>(), result, 0, List.of());
    }

    private static Layout collectShaped(ShapedCraftingRecipeDisplay shaped, ContextMap context) {
        int rw = shaped.width();
        int rh = shaped.height();
        List<SlotDisplay> ingredients = shaped.ingredients();
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < rw && i < GRID_DIM_MAX; i++) {
            for (int j = 0; j < rh && j < GRID_DIM_MAX; j++) {
                int index = i + j * rw;
                if (index >= ingredients.size())
                    continue;
                List<ItemStack> cycle = resolveCycle(ingredients.get(index), context);
                if (cycle.isEmpty())
                    continue;
                slots.add(new Slot(i, j, index, cycle));
            }
        }
        return new Layout(Kind.WORKBENCH_SHAPED, slots, shaped.result().resolveForFirstStack(context), 0, List.of());
    }

    private static Layout collectArcane(ArcaneCraftingRecipeDisplay arcane, ContextMap context) {
        List<SlotDisplay> ingredients = arcane.ingredients();
        List<Slot> slots = new ArrayList<>();
        List<ItemStack> crystals = new ArrayList<>();
        for (SlotDisplay crystal : arcane.crystals()) {
            ItemStack stack = crystal.resolveForFirstStack(context);
            if (!stack.isEmpty())
                crystals.add(stack);
        }
        if (arcane.shapeless()) {
            int cap = Math.min(ingredients.size(), 9);
            for (int i = 0; i < cap; i++) {
                List<ItemStack> cycle = resolveCycle(ingredients.get(i), context);
                if (cycle.isEmpty())
                    continue;
                slots.add(new Slot(i % GRID_DIM_MAX, i / GRID_DIM_MAX, i, cycle));
            }
            return new Layout(Kind.ARCANE_SHAPELESS, slots, arcane.result().resolveForFirstStack(context), arcane.visCost(), crystals);
        }
        int rw = arcane.width();
        int rh = arcane.height();
        for (int i = 0; i < rw && i < GRID_DIM_MAX; i++) {
            for (int j = 0; j < rh && j < GRID_DIM_MAX; j++) {
                int index = i + j * rw;
                if (index >= ingredients.size())
                    continue;
                List<ItemStack> cycle = resolveCycle(ingredients.get(index), context);
                if (cycle.isEmpty())
                    continue;
                slots.add(new Slot(i, j, index, cycle));
            }
        }
        return new Layout(Kind.ARCANE_SHAPED, slots, arcane.result().resolveForFirstStack(context), arcane.visCost(), crystals);
    }

    private static Layout collectShapeless(ShapelessCraftingRecipeDisplay shapeless, ContextMap context) {
        List<SlotDisplay> ingredients = shapeless.ingredients();
        List<Slot> slots = new ArrayList<>();
        int cap = Math.min(ingredients.size(), 9);
        for (int i = 0; i < cap; i++) {
            List<ItemStack> cycle = resolveCycle(ingredients.get(i), context);
            if (cycle.isEmpty())
                continue;
            slots.add(new Slot(i % GRID_DIM_MAX, i / GRID_DIM_MAX, i, cycle));
        }
        return new Layout(Kind.WORKBENCH_SHAPELESS, slots, shapeless.result().resolveForFirstStack(context), 0, List.of());
    }

    private static List<ItemStack> resolveCycle(SlotDisplay slot, ContextMap context) {
        List<ItemStack> cycle = slot.resolveForStacks(context);
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

    private static final float PREVIEW_ROT_X = 25.0F;
    private static final float PREVIEW_SPIN_DEG_PER_SEC = 7.5F;
    private static final float PREVIEW_SCALE = 15.0F;
    private static final int PREVIEW_HALF_WIDTH = 70;
    private static final int PREVIEW_TOP = -60;
    private static final int PREVIEW_BOTTOM = 84;

    private static final int ASPECT_CELL = 20;
    private static final int ASPECT_HALF_CELL = 10;

    private static void drawCruciblePage(GuiGraphicsExtractor graphics, int cx, int cy, CrucibleRecipeDisplay display, ContextMap context) {
        Font font = Minecraft.getInstance().font;
        drawKindLabel(graphics, font, cx, cy, "recipe.type.crucible");
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        blitOverlay(graphics, -28, CRUCIBLE_HEADER_Y, 0, 3, 56, 17);
        blitOverlay(graphics, -28, CRUCIBLE_BODY_Y, 0, 20, 56, 48);
        blitOverlay(graphics, CRUCIBLE_DRIP_X, CRUCIBLE_DRIP_Y, 100, 84, 11, 13);
        graphics.pose().popMatrix();
        drawAspectGrid(graphics, font, cx + CRUCIBLE_ASPECT_X, cy + CRUCIBLE_ASPECT_Y, display.aspects(), 3);
        ItemStack result = display.result().resolveForFirstStack(context);
        if (!result.isEmpty()) {
            graphics.item(result, cx + CRUCIBLE_RESULT_X, cy + CRUCIBLE_RESULT_Y);
        }
        ItemStack catalyst = pickRotating(resolveCycle(display.catalyst(), context), 0);
        if (!catalyst.isEmpty()) {
            graphics.item(catalyst, cx + CRUCIBLE_CATALYST_X, cy + CRUCIBLE_CATALYST_Y);
        }
    }

    private static void drawInfusionPage(GuiGraphicsExtractor graphics, int cx, int cy, InfusionRecipeDisplay display, ContextMap context) {
        Font font = Minecraft.getInstance().font;
        drawKindLabel(graphics, font, cx, cy, "recipe.type.infusion");
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy + INFUSION_PANEL_SHIFT_Y);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        blitOverlay(graphics, -28, INFUSION_HEADER_Y, 0, 3, 56, 17);
        blitOverlay(graphics, -28, INFUSION_BODY_Y, 200, 77, 56, 44);
        graphics.pose().popMatrix();
        drawAspectGrid(graphics, font, cx + INFUSION_ASPECT_X, cy + INFUSION_ASPECT_Y, display.aspects(), 5);
        ItemStack result = display.result().resolveForFirstStack(context);
        if (!result.isEmpty()) {
            graphics.item(result, cx + CRUCIBLE_RESULT_X, cy + INFUSION_RESULT_Y);
        }
        ItemStack catalyst = pickRotating(resolveCycle(display.catalyst(), context), 0);
        if (!catalyst.isEmpty()) {
            graphics.item(catalyst, cx + CRUCIBLE_RESULT_X, cy + INFUSION_CATALYST_Y);
        }
        List<SlotDisplay> components = display.components();
        for (int a = 0; a < components.size(); a++) {
            ItemStack stack = pickRotating(resolveCycle(components.get(a), context), a + 1);
            if (!stack.isEmpty()) {
                int[] offset = infusionRingOffset(a, components.size());
                graphics.item(stack, cx + offset[0], cy + INFUSION_RING_CENTER_Y + offset[1]);
            }
        }
        int inst = Math.min(INFUSION_INSTABILITY_MAX, display.instability() / 2);
        Component text = Component.translatable("tc.inst").append(Component.translatable("tc.inst." + inst));
        int offset = font.width(text);
        graphics.text(font, text, cx - offset / 2, cy + INFUSION_INSTABILITY_Y, LABEL_COLOR, false);
    }

    private static void drawConstructPage(GuiGraphicsExtractor graphics, int cx, int cy, MultiblockRecipeDisplay display, ContextMap context) {
        Font font = Minecraft.getInstance().font;
        drawKindLabel(graphics, font, cx, cy, "recipe.type.construct");
        drawSlotFrame(graphics, cx, cy);
        ItemStack result = display.result().resolveForFirstStack(context);
        if (!result.isEmpty()) {
            graphics.item(result, cx + OUTPUT_OFFSET_X, cy + OUTPUT_OFFSET_Y);
        }
        drawBlueprintPreview(graphics, cx, cy, display.blueprint());
        List<ItemStack> ingredients = blueprintIngredients(display.blueprint());
        for (int a = 0; a < ingredients.size(); a++) {
            int ix = cx + CONSTRUCT_INGREDIENT_X + a * CONSTRUCT_INGREDIENT_STRIDE;
            graphics.item(ingredients.get(a), ix, cy + CONSTRUCT_INGREDIENT_Y);
            graphics.itemDecorations(font, ingredients.get(a), ix, cy + CONSTRUCT_INGREDIENT_Y);
        }
    }

    private static void drawBlueprintPreview(GuiGraphicsExtractor graphics, int cx, int cy, Identifier blueprintId) {
        Blueprint blueprint = lookupBlueprint(blueprintId);
        if (blueprint == null) {
            return;
        }
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        for (int y = 0; y < blueprint.ySize(); y++) {
            for (int x = 0; x < blueprint.xSize(); x++) {
                for (int z = 0; z < blueprint.zSize(); z++) {
                    BlueprintPart part = blueprint.cell(y, x, z);
                    if (part != null) {
                        blocks.put(new BlockPos(x, -y + (blueprint.ySize() - 1), z), part.source().getState());
                    }
                }
            }
        }
        Matrix3x2f pose = graphics.pose();
        int px = Math.round(pose.m20);
        int py = Math.round(pose.m21);
        float rotY = System.currentTimeMillis() % 2_880_000L / 1000.0F * PREVIEW_SPIN_DEG_PER_SEC + 90.0F;
        ((GuiGraphicsExtractorAccessor) graphics).thaumaturge$getGuiRenderState().addPicturesInPictureState(new BlockPreviewRenderState(blocks, PREVIEW_ROT_X, rotY, 1, PREVIEW_SCALE, 0, 0,
                px + cx - PREVIEW_HALF_WIDTH, py + cy + PREVIEW_TOP, px + cx + PREVIEW_HALF_WIDTH, py + cy + PREVIEW_BOTTOM, null));
    }

    private static void drawKindLabel(GuiGraphicsExtractor graphics, Font font, int cx, int cy, String key) {
        Component text = Component.translatable(key);
        int offset = font.width(text);
        graphics.text(font, text, cx - offset / 2, cy + LABEL_OFFSET_Y, LABEL_COLOR, false);
    }

    private static void blitOverlay(GuiGraphicsExtractor graphics, int ox, int oy, int u, int v, int w, int h) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK_OVERLAY, ox, oy, (float) u, (float) v, w, h, w, h, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
    }

    private static List<AspectInstance> sortedAspects(AspectList aspects) {
        return aspects.entries().stream().sorted(Comparator.comparing(e -> e.aspect().getKey().identifier().toString())).toList();
    }

    private static void drawAspectGrid(GuiGraphicsExtractor graphics, Font font, int sx, int sy, AspectList aspects, int perRow) {
        Font chipFont = font;
        List<AspectInstance> sorted = sortedAspects(aspects);
        int rows = (sorted.size() - 1) / perRow;
        int startY = sy - ASPECT_HALF_CELL * rows;
        int total = 0;
        for (AspectInstance instance : sorted) {
            int[] pos = aspectCell(sx, startY, total, sorted.size(), perRow, rows);
            AspectTagRenderer.render(graphics, chipFont, pos[0], pos[1], instance.aspect(), instance.amount());
            total++;
        }
    }

    private static int[] aspectCell(int sx, int sy, int index, int count, int perRow, int rows) {
        int shift = (perRow - count % perRow) * ASPECT_HALF_CELL;
        int m = index / perRow >= rows && (rows > 1 || count < perRow) ? 1 : 0;
        return new int[]{sx + index % perRow * ASPECT_CELL + shift * m, sy + index / perRow * ASPECT_CELL};
    }

    private static int[] infusionRingOffset(int index, int count) {
        float pieSlice = 360.0F / count;
        float rot = -90.0F + pieSlice * index;
        int xx = (int) (Mth.cos(rot / 180.0F * (float) Math.PI) * INFUSION_RING_RADIUS) - 8;
        int yy = (int) (Mth.sin(rot / 180.0F * (float) Math.PI) * INFUSION_RING_RADIUS) - 8;
        return new int[]{xx, yy};
    }

    private static @Nullable Blueprint lookupBlueprint(Identifier blueprintId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        Registry<Blueprint> registry = mc.level.registryAccess().lookup(Blueprint.REGISTRY_KEY).orElse(null);
        if (registry == null) {
            return null;
        }
        return registry.get(ResourceKey.create(Blueprint.REGISTRY_KEY, blueprintId)).map(Holder::value).orElse(null);
    }

    private static List<ItemStack> blueprintIngredients(Identifier blueprintId) {
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
        counts.entrySet().stream().sorted(Comparator.comparingInt((Map.Entry<BlueprintSource, Integer> e) -> e.getValue()).reversed()).forEach(e -> {
            ItemStack stack = e.getKey().getRepresentations().getFirst().copy();
            stack.setCount(e.getValue());
            out.add(stack);
        });
        return out;
    }

    private static @Nullable ItemStack hoverCruciblePage(int cx, int cy, CrucibleRecipeDisplay display, ContextMap context, double mouseX, double mouseY) {
        ItemStack result = display.result().resolveForFirstStack(context);
        if (!result.isEmpty() && hitItem(cx + CRUCIBLE_RESULT_X, cy + CRUCIBLE_RESULT_Y, mouseX, mouseY)) {
            return result;
        }
        ItemStack catalyst = pickRotating(resolveCycle(display.catalyst(), context), 0);
        if (!catalyst.isEmpty() && hitItem(cx + CRUCIBLE_CATALYST_X, cy + CRUCIBLE_CATALYST_Y, mouseX, mouseY)) {
            return catalyst;
        }
        return null;
    }

    private static @Nullable ItemStack hoverInfusionPage(int cx, int cy, InfusionRecipeDisplay display, ContextMap context, double mouseX, double mouseY) {
        ItemStack result = display.result().resolveForFirstStack(context);
        if (!result.isEmpty() && hitItem(cx + CRUCIBLE_RESULT_X, cy + INFUSION_RESULT_Y, mouseX, mouseY)) {
            return result;
        }
        ItemStack catalyst = pickRotating(resolveCycle(display.catalyst(), context), 0);
        if (!catalyst.isEmpty() && hitItem(cx + CRUCIBLE_RESULT_X, cy + INFUSION_CATALYST_Y, mouseX, mouseY)) {
            return catalyst;
        }
        List<SlotDisplay> components = display.components();
        for (int a = 0; a < components.size(); a++) {
            int[] offset = infusionRingOffset(a, components.size());
            if (hitItem(cx + offset[0], cy + INFUSION_RING_CENTER_Y + offset[1], mouseX, mouseY)) {
                ItemStack stack = pickRotating(resolveCycle(components.get(a), context), a + 1);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return null;
    }

    private static @Nullable ItemStack hoverConstructPage(int cx, int cy, MultiblockRecipeDisplay display, ContextMap context, double mouseX, double mouseY) {
        ItemStack result = display.result().resolveForFirstStack(context);
        if (!result.isEmpty() && hitItem(cx + OUTPUT_OFFSET_X, cy + OUTPUT_OFFSET_Y, mouseX, mouseY)) {
            return result;
        }
        List<ItemStack> ingredients = blueprintIngredients(display.blueprint());
        for (int a = 0; a < ingredients.size(); a++) {
            if (hitItem(cx + CONSTRUCT_INGREDIENT_X + a * CONSTRUCT_INGREDIENT_STRIDE, cy + CONSTRUCT_INGREDIENT_Y, mouseX, mouseY)) {
                return ingredients.get(a);
            }
        }
        return null;
    }

    private static boolean hitItem(int x, int y, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + ITEM_HIT_SIZE && mouseY >= y && mouseY < y + ITEM_HIT_SIZE;
    }

    private static @Nullable Component hoverAspectGrid(int sx, int sy, List<AspectInstance> sorted, int perRow, double mouseX, double mouseY) {
        int rows = (sorted.size() - 1) / perRow;
        int startY = sy - ASPECT_HALF_CELL * rows;
        for (int index = 0; index < sorted.size(); index++) {
            int[] pos = aspectCell(sx, startY, index, sorted.size(), perRow, rows);
            if (hitItem(pos[0], pos[1], mouseX, mouseY)) {
                AspectInstance instance = sorted.get(index);
                return AspectComponents.name(instance.aspect()).append("\n").append(AspectComponents.description(instance.aspect()));
            }
        }
        return null;
    }

    private static ItemStack pickRotating(List<ItemStack> stacks, int counter) {
        if (stacks.isEmpty())
            return ItemStack.EMPTY;
        long wall = System.currentTimeMillis() / CYCLE_SECONDS;
        int index = (int) Math.floorMod((long) counter + wall, (long) stacks.size());
        return stacks.get(index);
    }

    private enum Kind {
        WORKBENCH_SHAPED, WORKBENCH_SHAPELESS, ARCANE_SHAPED, ARCANE_SHAPELESS, UNKNOWN
    }

    private record Slot(int col, int row, int counter, List<ItemStack> cycle) {
    }

    private record Layout(Kind kind, List<Slot> slots, ItemStack output, int visCost, List<ItemStack> crystals) {
    }
}
