package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumaturge.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class CrucibleCategory implements IRecipeCategory<RecipeHolder<CrucibleRecipe>> {
    public static final IRecipeHolderType<CrucibleRecipe> RECIPE_TYPE = IRecipeHolderType.create(TCRecipeTypes.CRUCIBLE.get());

    public static final int ASPECT_Y = 66;
    public static final int ASPECT_X = 66;
    public static final int SPACE = 22;
    private static final IDrawable background = new AlphaDrawable(Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"), 2, 5, 109, 129, 0, 0, 9, 10);;
    private static final IDrawable arrow = new AlphaDrawable(Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"), 199, 168, 26, 26);

    private final IDrawable icon;

    public CrucibleCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TCItems.CRUCIBLE.get()));
    }

    @Override
    public IRecipeType<RecipeHolder<CrucibleRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.type.crucible");
    }

    @Override
    public int getWidth() {
        return 129;
    }

    @Override
    public int getHeight() {
        return 129;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CrucibleRecipe> holder, IFocusGroup focuses) {
        CrucibleRecipe recipe = holder.value();
        builder.addOutputSlot(55, 8).add(recipe.rawResult());
        builder.addInputSlot(2, 2).add(recipe.catalyst());

        int center = (recipe.aspects().size() * SPACE) / 2;
        int x = 0;

        for (AspectInstance instance : recipe.aspects().sortedByAmount()) {
            builder.addInputSlot(ASPECT_X - center + x * SPACE, ASPECT_Y).setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE).add(AspectIngredientType.INSTANCE,
                    instance);

            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(PhialItem.makeFilled(instance.aspect()));
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(EssentiaCrystalFactory.of(instance.aspect()));
            x += 1;
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<CrucibleRecipe> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        Optional<ResearchGate> gate = recipe.value().researchGate();
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate && mouseX > 22 && mouseX < 40 && mouseY > 14 && mouseY < 30) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(gate.get()));
        }
    }

    @Override
    public void draw(RecipeHolder<CrucibleRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        arrow.draw(guiGraphics, 16, 6);
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate)
            guiGraphics.item(Items.BARRIER.getDefaultInstance(), 22, 14);
    }
}
