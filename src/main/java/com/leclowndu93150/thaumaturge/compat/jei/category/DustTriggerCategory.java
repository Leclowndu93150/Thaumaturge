package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class DustTriggerCategory implements IRecipeCategory<RecipeHolder<DustTrigger>> {
    public static final IRecipeHolderType<DustTrigger> RECIPE_TYPE = IRecipeHolderType.create(TCRecipeTypes.DUST_TRIGGER.get());

    private static final int WIDTH = 144;
    private static final int HEIGHT = 54;

    private static final int DUST_SLOT_X = 6;
    private static final int DUST_SLOT_Y = 18;
    private static final int TARGET_SLOT_X = 56;
    private static final int TARGET_SLOT_Y = 18;
    private static final int RESULT_SLOT_X = 118;
    private static final int RESULT_SLOT_Y = 18;

    private final IDrawable icon;

    private final IDrawable resultIcon = new AlphaDrawable(Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"), 41, 7, 30, 30);

    public DustTriggerCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TCItems.SALIS_MUNDUS.get()));
    }

    @Override
    public IRecipeType<RecipeHolder<DustTrigger>> getRecipeType() {
        return RECIPE_TYPE;
    }

    public static IRecipeType<RecipeHolder<DustTrigger>> type() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.thaumaturge.category.dust_trigger");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DustTrigger> holder, IFocusGroup focuses) {

        Component usage = Component.translatable("jei.thaumaturge.dust_trigger.usage");
        builder.addSlot(RecipeIngredientRole.INPUT, DUST_SLOT_X + 1, DUST_SLOT_Y + 1).add(TCItems.SALIS_MUNDUS.get()).addRichTooltipCallback((view, tooltip) -> tooltip.add(usage));

        DustTrigger recipe = holder.value();
        IRecipeSlotBuilder targetSlot = builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, TARGET_SLOT_X + 1, TARGET_SLOT_Y + 1);
        if (recipe instanceof DustTriggerSimpleRecipe simple) {
            targetSlot.add(new ItemStack(simple.target()));
        } else if (recipe instanceof DustTriggerTagRecipe tagRecipe) {
            TagKey<Block> tag = tagRecipe.targetTag();
            List<ItemStack> stacks = stacksFromBlockTag(tag);
            if (!stacks.isEmpty()) {
                targetSlot.addItemStacks(stacks);
            }
            Component tagLabel = Component.translatable("jei.thaumaturge.dust_trigger.target.tag", Component.literal("#" + tag.location()));
            targetSlot.addRichTooltipCallback((view, tooltip) -> tooltip.add(tagLabel));
        }

        ItemStack result = resultStack(recipe);
        if (!result.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_SLOT_X + 1, RESULT_SLOT_Y + 1).add(result);
        }
    }

    private static List<ItemStack> stacksFromBlockTag(TagKey<Block> tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            ItemStack stack = new ItemStack(blockHolder.value());
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    @Override
    public void draw(RecipeHolder<DustTrigger> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        resultIcon.draw(guiGraphics, RESULT_SLOT_X - 6, RESULT_SLOT_Y - 6);

        Font font = Minecraft.getInstance().font;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(2);
        guiGraphics.text(font, "+", (((DUST_SLOT_X + 16) + TARGET_SLOT_X) / 2 - 5) / 2, 20 / 2, 0xFF000000 | ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.text(font, "=", (((TARGET_SLOT_X + 16) + RESULT_SLOT_X) / 2 - 5) / 2, 20 / 2, 0xFF000000 | ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.pose().popMatrix();

        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate)
            guiGraphics.item(Items.BARRIER.getDefaultInstance(), ((TARGET_SLOT_X + 16) + RESULT_SLOT_X) / 2 - 8, 20);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<DustTrigger> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        Optional<ResearchGate> gate = recipe.value().researchGate();
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate && mouseX > (double) ((TARGET_SLOT_X + 16) + RESULT_SLOT_X) / 2 - 8 && mouseX < (double) ((TARGET_SLOT_X + 16) + RESULT_SLOT_X) / 2 + 10 && mouseY > 20 && mouseY < 36) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(gate.get()));
        }
    }

    private static @Nullable TagKey<Block> targetTag(DustTrigger recipe) {
        if (recipe instanceof DustTriggerTagRecipe tag) {
            return tag.targetTag();
        }
        return null;
    }

    static ItemStack resultStack(DustTrigger recipe) {
        if (recipe instanceof DustTriggerSimpleRecipe simple) {
            return simple.result();
        }
        if (recipe instanceof DustTriggerTagRecipe tag) {
            return tag.result();
        }
        if (recipe instanceof DustTriggerMultiblockRecipe multi) {
            return multi.result();
        }
        return ItemStack.EMPTY;
    }
}
