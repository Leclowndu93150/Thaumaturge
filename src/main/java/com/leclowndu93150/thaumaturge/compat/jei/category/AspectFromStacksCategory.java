package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class AspectFromStacksCategory implements IRecipeCategory<AspectFromStacksCategory.Wrapper> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "aspect_from_stacks");
    public static final RecipeType<Wrapper> RECIPE_TYPE = new RecipeType<>(UID, Wrapper.class);

    private static final int WIDTH = 100;
    private static final int HEIGHT = 26;

    private static final int LEFT_X = 6;
    private static final int LEFT_Y = 5;
    private static final int RIGHT_X = 38;
    private static final int RIGHT_Y = 5;
    private static final int RESULT_X = 78;
    private static final int RESULT_Y = 5;

    private final IDrawable icon;

    private final IDrawable resultSlot = new AlphaDrawable(
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/gui/gui_researchbook_overlay.png"),
            40,
            6,
            32,
            32,
            0,
            18 * 4 + 5,
            72,
            72);

    public AspectFromStacksCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TTItems.THAUMONOMICON.get()));
    }

    @Override
    public RecipeType<Wrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.thaumaturge.category.aspect_from_stacks");
    }

    @Override
    public int getWidth() {
        return 176;
    }

    @Override
    public int getHeight() {
        return 109;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Wrapper recipe, IFocusGroup focuses) {
        builder.addOutputSlot(8 + 81 - 9, 8)
                .setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE)
                .addIngredient(AspectIngredientType.INSTANCE, new AspectInstance(recipe.aspect(), 1));

        int slot = 0;
        int row = 9;
        for (ItemStack stack : recipe.stacks()) {
            builder.addInputSlot((slot % row) * 18 - 18 * 3 - 21 + 81, (slot / row) * 18 + 32)
                    .addItemStack(stack);
            ++slot;
        }
    }

    @Override
    public void draw(
            Wrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        GuiBlend.withAlphaBlend(guiGraphics, () -> {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            resultSlot.draw(guiGraphics);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
            guiGraphics.blit(
                    ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/gui/gui_inner.png"),
                    5,
                    30,
                    0,
                    0,
                    163,
                    74,
                    256,
                    256);
        });
    }

    public record Wrapper(Holder<IAspect> aspect, List<ItemStack> stacks) {}
}
