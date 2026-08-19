package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class AspectCompositionCategory implements IRecipeCategory<AspectCompositionCategory.Composition> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(TCIds.MODID, "aspect_composition");
    public static final IRecipeType<Composition> RECIPE_TYPE = IRecipeType.create(UID, Composition.class);

    private static final int WIDTH = 100;
    private static final int HEIGHT = 26;

    private static final int LEFT_X = 6;
    private static final int LEFT_Y = 5;
    private static final int RIGHT_X = 38;
    private static final int RIGHT_Y = 5;
    private static final int RESULT_X = 78;
    private static final int RESULT_Y = 5;

    private final IDrawable icon;

    public AspectCompositionCategory(IGuiHelper guiHelper, @Nullable Holder<IAspect> iconAspect) {
        if (iconAspect != null) {
            this.icon = guiHelper.createDrawableIngredient(AspectIngredientType.INSTANCE, new AspectInstance(iconAspect, 1));
        } else {
            this.icon = guiHelper.createDrawableItemStack(new ItemStack(TCItems.SALIS_MUNDUS.get()));
        }
    }

    @Override
    public IRecipeType<Composition> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.thaumaturge.category.aspect_composition");
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
    public void setRecipe(IRecipeLayoutBuilder builder, Composition recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, LEFT_X, LEFT_Y).setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE).add(AspectIngredientType.INSTANCE,
                new AspectInstance(recipe.left(), 1));
        builder.addSlot(RecipeIngredientRole.INPUT, RIGHT_X, RIGHT_Y).setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE).add(AspectIngredientType.INSTANCE,
                new AspectInstance(recipe.right(), 1));
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, RESULT_Y).setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE).add(AspectIngredientType.INSTANCE,
                new AspectInstance(recipe.result(), 1));
    }

    @Override
    public void draw(Composition recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        guiGraphics.text(font, "+", ((LEFT_X + 16) + RIGHT_X) / 2 - 3, 10, 0xFF000000 | ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.text(font, "=", ((RIGHT_X + 16) + RESULT_X) / 2 - 3, 10, 0xFF000000 | ChatFormatting.DARK_GRAY.getColor(), false);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.5F);
        Component left = AspectComponents.shortName(recipe.left());
        int leftWidth = font.width(left) / 2;
        guiGraphics.text(font, left, (LEFT_X + 8 - (leftWidth / 2)) * 2, 45, 0xFF000000 | ChatFormatting.DARK_GRAY.getColor(), false);

        Component right = AspectComponents.shortName(recipe.right());
        int rightWidth = font.width(right) / 2;
        guiGraphics.text(font, right, (RIGHT_X + 8 - (rightWidth / 2)) * 2, 45, 0xFF000000 | ChatFormatting.DARK_GRAY.getColor(), false);

        Component result = AspectComponents.shortName(recipe.result());
        int resultWidth = font.width(result) / 2;
        guiGraphics.text(font, result, (RESULT_X + 8 - (resultWidth / 2)) * 2, 45, 0xFF000000 | ChatFormatting.DARK_GRAY.getColor(), false);

        guiGraphics.pose().popMatrix();
    }

    public static List<Composition> collect(Iterable<Holder.Reference<IAspect>> all) {
        ArrayList<Composition> out = new ArrayList<>();
        for (Holder.Reference<IAspect> result : all) {
            IAspect value = result.value();
            if (value.isPrimal()) {
                continue;
            }
            List<Holder<IAspect>> components = value.components();
            if (components.size() != 2) {
                continue;
            }
            out.add(new Composition(components.get(0), components.get(1), result));
        }
        return out;
    }

    public record Composition(Holder<IAspect> left, Holder<IAspect> right, Holder<IAspect> result) {
    }
}
