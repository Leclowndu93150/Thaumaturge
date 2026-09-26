package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.jei.category.InfernalFurnaceCategory.InfernalBonusWrapper;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.InfernalBonus;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public final class InfernalFurnaceCategory implements IRecipeCategory<InfernalBonusWrapper> {
    public static final IRecipeType<InfernalBonusWrapper> RECIPE_TYPE = IRecipeType.create(Identifier.fromNamespaceAndPath(TCIds.MODID, "infernal_furnace"), InfernalBonusWrapper.class);

    private static final int WIDTH = 144;
    private static final int HEIGHT = 108;

    private static final IDrawable resultIcon = new AlphaDrawable(Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"), 41, 7, 30, 30);
    private static final IDrawable arrow = new AlphaDrawable(Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"), 199, 168, 26, 26);
    private static final IDrawable furnace = new AlphaDrawable(Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"), 445, 452, 67, 60);

    private final IDrawable icon;

    private static final int INPUT_SLOT_X = WIDTH / 2 - arrow.getWidth() / 2 - 20 - 18;
    private static final int INPUT_SLOT_Y = 6;
    private static final int RESULT_SLOT_X = 95;
    private static final int RESULT_SLOT_Y = HEIGHT / 2;

    public InfernalFurnaceCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TCItems.INFERNAL_FURNACE.get()));
    }

    @Override
    public IRecipeType<InfernalBonusWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    public static IRecipeType<InfernalBonusWrapper> type() {
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

        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_SLOT_X + 1, INPUT_SLOT_Y + 1).add(wrapper.ingredient());

        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_SLOT_X + 1, RESULT_SLOT_Y + 1).add(wrapper.defaultOutput());

        for (InfernalBonus bonus : wrapper.bonuses()) {
            Component chance = Component.translatable("gui.jei.category.compostable.chance", bonus.chance() * 100);
            Component count = Component.translatable("jei.thaumaturge.infernal_furnace.count", bonus.count().toString());
            builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_SLOT_X + 28, 9 + 20 * wrapper.bonuses().indexOf(bonus)).add(Ingredient.of(bonus.items())).addRichTooltipCallback((view, tooltip) -> {
                tooltip.add(chance.copy().withStyle(ChatFormatting.GRAY));
                tooltip.add(count.copy().withStyle(ChatFormatting.GRAY));
            });
        }
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(InfernalBonusWrapper wrapper, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        resultIcon.draw(guiGraphics, RESULT_SLOT_X - 6, RESULT_SLOT_Y - 6);
        arrow.draw(guiGraphics, WIDTH / 2 - arrow.getWidth() / 2 - 20, 9);
        furnace.draw(guiGraphics, WIDTH / 2 - furnace.getWidth() / 2 - 18, HEIGHT / 2 - furnace.getHeight() / 2 + 9);
    }

    public static record InfernalBonusWrapper(Ingredient ingredient, ItemStack defaultOutput, List<InfernalBonus> bonuses) {

    }
}
