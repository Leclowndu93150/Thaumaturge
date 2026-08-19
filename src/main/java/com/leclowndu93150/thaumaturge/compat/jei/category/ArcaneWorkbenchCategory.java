package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapelessCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchPayment;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class ArcaneWorkbenchCategory implements IRecipeCategory<RecipeHolder<ArcaneCraftingRecipe>> {
    public static final IRecipeHolderType<ArcaneCraftingRecipe> RECIPE_TYPE = IRecipeHolderType.create(TCRecipeTypes.ARCANE.get());

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png");

    private static final int WIDTH = 162;
    private static final int HEIGHT = 138;

    private static final int GRID_ORIGIN_X = 42;
    private static final int GRID_ORIGIN_Y = 48;
    private static final int GRID_SPACING = 31;

    private static final int OUTPUT_X = 73;
    private static final int OUTPUT_Y = 7;

    private static final int CRYSTAL_X = 141;
    private static final int CRYSTAL_Y = 6;
    private static final int CRYSTAL_SPACING = 22;

    private static final int PLATE_X = 65;
    private static final int PLATE_Y = 0;
    private static final int ARROW_X = 12;
    private static final int ARROW_Y = 4;
    private static final int BARRIER_X = 15;
    private static final int BARRIER_Y = 8;

    private static final int VIS_CENTER_X = 50;
    private static final int VIS_Y = 12;

    private static final int RESEARCH_ZONE_MIN_X = 10;
    private static final int RESEARCH_ZONE_MAX_X = 34;
    private static final int VIS_ZONE_MIN_X = 34;
    private static final int VIS_ZONE_MAX_X = 60;
    private static final int TOP_ZONE_MIN_Y = 4;
    private static final int TOP_ZONE_MAX_Y = 28;

    private final IDrawable background = new AlphaDrawable(TEXTURE, 225, 31, 102, 102, 36, 0, 30, 30);
    private final IDrawable plate = new AlphaDrawable(TEXTURE, 40, 6, 32, 32);
    private final IDrawable arrow = new AlphaDrawable(TEXTURE, 135, 152, 23, 23);
    private final IDrawable icon;

    public ArcaneWorkbenchCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TCItems.ARCANE_WORKBENCH.get()));
    }

    @Override
    public IRecipeType<RecipeHolder<ArcaneCraftingRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.thaumaturge.category.arcane_workbench");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ArcaneCraftingRecipe> holder, IFocusGroup focuses) {
        ArcaneCraftingRecipe recipe = holder.value();
        ItemStackTemplate output;

        if (recipe instanceof ArcaneShapedCraftingRecipe shaped) {
            output = shaped.result();
            layoutShaped(builder, shaped);
        } else {
            ArcaneShapelessCraftingRecipe shapeless = (ArcaneShapelessCraftingRecipe) recipe;
            output = shapeless.result();
            builder.setShapeless();
            layoutShapeless(builder, shapeless);
        }

        layoutCrystals(builder, recipe.getCrystals());
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).add(output);
    }

    private void layoutShaped(IRecipeLayoutBuilder builder, ArcaneShapedCraftingRecipe recipe) {
        int width = recipe.getWidth();
        int height = recipe.getHeight();
        List<Optional<Ingredient>> ingredients = recipe.getIngredients();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Optional<Ingredient> ingredient = ingredients.get(x + y * width);
                IRecipeSlotBuilder slot = builder.addInputSlot(GRID_ORIGIN_X + x * GRID_SPACING, GRID_ORIGIN_Y + y * GRID_SPACING);
                if (ingredient.isPresent())
                    slot.add(ingredient.get());
            }
        }
    }

    private void layoutShapeless(IRecipeLayoutBuilder builder, ArcaneShapelessCraftingRecipe recipe) {
        List<Ingredient> ingredients = recipe.ingredients();
        for (int i = 0; i < 9; i++) {
            int x = i % 3;
            int y = i / 3;
            IRecipeSlotBuilder slot = builder.addInputSlot(GRID_ORIGIN_X + x * GRID_SPACING, GRID_ORIGIN_Y + y * GRID_SPACING);
            try {
                slot.add(ingredients.get(i));
            } catch (Exception ignored) {
            }
        }
    }

    private void layoutCrystals(IRecipeLayoutBuilder builder, AspectList crystals) {
        if (crystals.isEmpty())
            return;
        int index = 0;
        List<AspectInstance> aspects = crystals.entries().stream().sorted(Comparator.comparingInt(k -> MenuArcaneWorkbench.PRIMAL_ORDER.indexOf(k.aspect().getKey()))).toList();
        for (int i = 0; i < 6; i++) {
            boolean isAdded = Objects.equals(aspects.get(index).aspect().getKey(), MenuArcaneWorkbench.PRIMAL_ORDER.get(i));
            if (isAdded) {
                IRecipeSlotBuilder slot = builder.addInputSlot(CRYSTAL_X, CRYSTAL_Y + index * CRYSTAL_SPACING);
                AspectInstance instance = aspects.get(index);
                slot.add(EssentiaCrystalFactory.of(instance.aspect(), instance.amount()));
                index++;
                if (index >= aspects.size())
                    break;
            } else {
                builder.addInputSlot(BARRIER_X, BARRIER_Y);
            }
        }
    }

    @Override
    public void draw(RecipeHolder<ArcaneCraftingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        plate.draw(guiGraphics, PLATE_X, PLATE_Y);
        arrow.draw(guiGraphics, ARROW_X, ARROW_Y);

        Font font = Minecraft.getInstance().font;
        String vis = Integer.toString(recipe.value().getBaseVis());
        guiGraphics.text(font, vis, VIS_CENTER_X - font.width(vis) / 2, VIS_Y, 0xFF000000 | ChatFormatting.DARK_GRAY.getColor(), false);

        if (!recipe.value().doesPassGate(Minecraft.getInstance().player)) {
            guiGraphics.item(Items.BARRIER.getDefaultInstance(), BARRIER_X, BARRIER_Y);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<ArcaneCraftingRecipe> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        boolean inTopRow = mouseY > TOP_ZONE_MIN_Y && mouseY < TOP_ZONE_MAX_Y;
        if (inTopRow && mouseX > VIS_ZONE_MIN_X && mouseX < VIS_ZONE_MAX_X) {
            tooltip.add(Component.translatable("jei.thaumaturge.arcane_workbench.vis_cost"));
            tooltip.add(Component.translatable("jei.thaumaturge.arcane_workbench.vis_cost_wand", WandEconomy.CRYSTAL_SUBSTITUTE_VIS));
            tooltip.add(Component.translatable("jei.thaumaturge.arcane_workbench.vis_cost_aura", WorkbenchPayment.crudeCost(recipe.value())));
            return;
        }

        Optional<ResearchGate> gate = recipe.value().researchGate();
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate && gate.isPresent() && inTopRow && mouseX > RESEARCH_ZONE_MIN_X && mouseX < RESEARCH_ZONE_MAX_X) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(gate.get()));
        }
    }
}
