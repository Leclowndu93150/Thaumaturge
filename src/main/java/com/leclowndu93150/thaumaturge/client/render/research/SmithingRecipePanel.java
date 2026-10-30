package com.leclowndu93150.thaumaturge.client.render.research;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

final class SmithingRecipePanel implements RecipePanel {
    private static final String LABEL_KEY = "gui.thaumaturge.recipe_type.smithing";
    private static final int SKETCH_U = 54;
    private static final int SKETCH_V = 120;
    private static final int SKETCH_W = 68;
    private static final int SKETCH_H = 54;
    private static final int SKETCH_X = -34;
    private static final int SKETCH_Y = -30;
    private static final int INPUT_Y = -22;
    private static final int TEMPLATE_X = -40;
    private static final int BASE_X = -8;
    private static final int ADDITION_X = 24;
    private static final int TEMPLATE_CYCLE = 0;
    private static final int BASE_CYCLE = 1;
    private static final int ADDITION_CYCLE = 2;

    private final SlotDisplay template;
    private final SlotDisplay base;
    private final SlotDisplay addition;
    private final SlotDisplay result;

    SmithingRecipePanel(SlotDisplay template, SlotDisplay base, SlotDisplay addition, SlotDisplay result) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PanelContext context, float rotation, int layer) {
        context.drawLabel(graphics, LABEL_KEY);
        context.beginScaled(graphics, 0);
        context.blitScaled(graphics, SKETCH_U, SKETCH_V, SKETCH_W, SKETCH_H, SKETCH_X, SKETCH_Y, PanelContext.WHITE);
        context.endScaled(graphics);
        context.drawOutputFrame(graphics);
        for (RecipeDisplayWidget.ItemHit hit : hits(context)) {
            context.drawItem(graphics, hit);
        }
    }

    @Override
    public List<RecipeDisplayWidget.ItemHit> hits(PanelContext context) {
        ItemStack baseStack = context.pick(base, BASE_CYCLE);
        ItemStack additionStack = context.pick(addition, ADDITION_CYCLE);
        List<RecipeDisplayWidget.ItemHit> hits = new ArrayList<>();
        context.collect(hits, context.pick(template, TEMPLATE_CYCLE), TEMPLATE_X, INPUT_Y);
        context.collect(hits, baseStack, BASE_X, INPUT_Y);
        context.collect(hits, additionStack, ADDITION_X, INPUT_Y);
        context.collect(hits, resultFor(context, baseStack, additionStack), PanelContext.OUTPUT_X, PanelContext.OUTPUT_Y);
        return hits;
    }

    @Override
    public @Nullable List<Component> popup(PanelContext context, double mouseX, double mouseY) {
        return null;
    }

    private ItemStack resultFor(PanelContext context, ItemStack baseStack, ItemStack additionStack) {
        if (result instanceof SlotDisplay.SmithingTrimDemoSlotDisplay trim && !baseStack.isEmpty() && !additionStack.isEmpty()) {
            return SmithingTrimRecipe.applyTrim(baseStack, additionStack, trim.pattern());
        }
        return context.first(result);
    }
}
