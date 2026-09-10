package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.InfernalBonus;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class InfernalFurnaceCategory implements IRecipeCategory<InfernalFurnaceCategory.InfernalBonusWrapper> {
    public static final RecipeType<InfernalBonusWrapper> RECIPE_TYPE =
            RecipeType.create(TCIds.MODID, "infernal_furnace", InfernalBonusWrapper.class);

    private static final int WIDTH = 144;
    private static final int HEIGHT = 108;
    private static final IDrawable RESULT_ICON = new AlphaDrawable(
            ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"),
            41,
            7,
            30,
            30);
    private static final IDrawable ARROW = new AlphaDrawable(
            ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"),
            199,
            168,
            26,
            26);
    private static final IDrawable FURNACE = new AlphaDrawable(
            ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"),
            445,
            452,
            67,
            60);
    private static final int INPUT_SLOT_X = WIDTH / 2 - ARROW.getWidth() / 2 - 38;
    private static final int INPUT_SLOT_Y = 6;
    private static final int RESULT_SLOT_X = 95;
    private static final int RESULT_SLOT_Y = HEIGHT / 2;

    private final IDrawable icon;

    public InfernalFurnaceCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TCItems.INFERNAL_FURNACE.get()));
    }

    @Override
    public RecipeType<InfernalBonusWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.thaumaturge.category.infernal_furnace");
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
    public void setRecipe(IRecipeLayoutBuilder builder, InfernalBonusWrapper wrapper, IFocusGroup focuses) {
        builder.addInputSlot(INPUT_SLOT_X + 1, INPUT_SLOT_Y + 1).addIngredients(wrapper.ingredient());
        builder.addOutputSlot(RESULT_SLOT_X + 1, RESULT_SLOT_Y + 1).addItemStack(wrapper.defaultOutput());
        for (int index = 0; index < wrapper.bonuses().size(); index++) {
            InfernalBonus bonus = wrapper.bonuses().get(index);
            Component chance = Component.translatable("gui.jei.category.compostable.chance", bonus.chance() * 100);
            Component count = Component.translatable("jei.thaumaturge.infernal_furnace.count", countText(bonus));
            builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_SLOT_X + 28, 9 + 20 * index)
                    .addIngredients(Ingredient.of(bonus.items().stream().map(holder -> new ItemStack(holder.value()))))
                    .addRichTooltipCallback((view, tooltip) -> addBonusTooltip(tooltip, chance, count));
        }
    }

    private static String countText(InfernalBonus bonus) {
        int min = bonus.count().getMinValue();
        int max = bonus.count().getMaxValue();
        return min == max ? Integer.toString(min) : min + "-" + max;
    }

    private static void addBonusTooltip(ITooltipBuilder tooltip, Component chance, Component count) {
        tooltip.add(chance.copy().withStyle(ChatFormatting.GRAY));
        tooltip.add(count.copy().withStyle(ChatFormatting.GRAY));
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(
            InfernalBonusWrapper wrapper,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY) {
        RESULT_ICON.draw(guiGraphics, RESULT_SLOT_X - 6, RESULT_SLOT_Y - 6);
        ARROW.draw(guiGraphics, WIDTH / 2 - ARROW.getWidth() / 2 - 20, 9);
        FURNACE.draw(guiGraphics, WIDTH / 2 - FURNACE.getWidth() / 2 - 18, HEIGHT / 2 - FURNACE.getHeight() / 2 + 9);
    }

    public record InfernalBonusWrapper(Ingredient ingredient, ItemStack defaultOutput, List<InfernalBonus> bonuses) {}
}
