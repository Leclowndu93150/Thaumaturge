package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.DynamicInfusionRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionRecipePreview;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumaturge.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionEnchantmentRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.spell.item.SpellPartIngredient;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class InfusionCategory<R extends Recipe<?> & IInfusionRecipe> implements IRecipeCategory<RecipeHolder<R>> {
    public static final IRecipeHolderType<InfusionRecipe> RECIPE_TYPE = IRecipeHolderType.create(TTRecipeTypes.INFUSION.get());
    public static final IRecipeHolderType<DynamicInfusionRecipe> DYNAMIC_RECIPE_TYPE = IRecipeHolderType.create(TTRecipeTypes.DYNAMIC_INFUSION.get());
    public static final IRecipeHolderType<InfusionEnchantmentRecipe> ENCHANTMENT_RECIPE_TYPE = IRecipeHolderType.create(TTRecipeTypes.INFUSION_ENCHANTMENT.get());
    public static final IRecipeHolderType<InfusionRunicAugmentRecipe> RUNIC_RECIPE_TYPE = IRecipeHolderType.create(TTRecipeTypes.RUNIC_AUGMENT.get());

    public static final int ASPECT_Y = 135;
    public static final int ASPECT_X = 46;
    public static final int SPACE = 22;

    private static final Identifier TEXTURE = TTIds.rl("textures/gui/gui_researchbook_overlay.png");
    private static final int WIDTH = 146;
    private static final int HEIGHT = 170;
    private static final int BACKDROP_U = 413;
    private static final int BACKDROP_V = 154;
    private static final int BACKDROP_SIZE = 86;
    private static final int BACKDROP_PAD_TOP = 40;
    private static final int BACKDROP_PAD_BOTTOM = 44;
    private static final int BACKDROP_PAD_SIDE = 30;
    private static final int PLATE_U = 40;
    private static final int PLATE_V = 6;
    private static final int PLATE_SIZE = 32;
    private static final int PLATE_X = 57;
    private static final int PLATE_Y = 0;
    private static final int OUTPUT_X = 65;
    private static final int OUTPUT_Y = 7;
    private static final int CENTER_X = 65;
    private static final int CENTER_Y = 75;
    private static final int RING_RADIUS = 40;
    private static final double FULL_TURN_DEGREES = 360.0;
    private static final double RING_START_DEGREES = -90.0;
    private static final int ASPECT_ROW_OFFSET = 30;
    private static final int ASPECT_ROW_HALVES = 2;
    private static final int LOCK_X = 92;
    private static final int LOCK_Y = 9;
    private static final int LOCK_SIZE = 16;
    private static final int INSTABILITY_CENTER_X = 73;
    private static final int INSTABILITY_Y = 158;
    private static final int INSTABILITY_COLOR = 0xFF504030;
    private static final int INSTABILITY_DIVISOR = 2;
    private static final String INSTABILITY_KEY = "gui.thaumaturge.infusion.instability";

    private static final IDrawable BACKDROP = new AlphaDrawable(TEXTURE, BACKDROP_U, BACKDROP_V, BACKDROP_SIZE, BACKDROP_SIZE, BACKDROP_PAD_TOP, BACKDROP_PAD_BOTTOM, BACKDROP_PAD_SIDE,
            BACKDROP_PAD_SIDE);
    private static final IDrawable PLATE = new AlphaDrawable(TEXTURE, PLATE_U, PLATE_V, PLATE_SIZE, PLATE_SIZE);

    private final IDrawable icon;
    private final Component title;
    private final IRecipeHolderType<R> recipeType;

    public InfusionCategory(IGuiHelper guiHelper, IRecipeHolderType<R> recipeType, String titleKey) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TTItems.INFUSION_MATRIX.get()));
        this.title = Component.translatable(titleKey);
        this.recipeType = recipeType;
    }

    @Override
    public IRecipeType<RecipeHolder<R>> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
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
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<R> holder, IFocusGroup focuses) {
        R recipe = holder.value();
        List<AspectInstance> aspects;
        if (recipe instanceof DynamicInfusionRecipe dynamic) {
            Optional<InfusionRecipePreview> preview = preview(dynamic);
            if (preview.isEmpty()) {
                return;
            }
            InfusionRecipePreview display = preview.get();
            aspects = display.essentia().sortedByAmount();
            builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).add(display.output());
            builder.addInputSlot(CENTER_X, CENTER_Y).add(display.input().catalyst());
            List<ItemStack> components = display.input().components();
            double step = FULL_TURN_DEGREES / components.size();
            for (int index = 0; index < components.size(); index++) {
                double angle = Math.toRadians(RING_START_DEGREES + index * step);
                int x = CENTER_X + (int) (RING_RADIUS * Math.cos(angle));
                int y = CENTER_Y + (int) (RING_RADIUS * Math.sin(angle));
                builder.addInputSlot(x, y).add(components.get(index));
            }
        } else {
            aspects = recipe.aspects().sortedByAmount();
            builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).add(recipe.resultItem());
            addIngredientSlot(builder, CENTER_X, CENTER_Y, recipe.catalyst());
            addRingSlots(builder, recipe.components());
        }
        addAspectSlots(builder, aspects);

        addHiddenInputs(builder, aspects, PhialItem::makeFilled);
        addHiddenInputs(builder, aspects, EssentiaCrystalFactory::of);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<R> holder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        R recipe = holder.value();
        boolean unlocked = recipe.doesPassGate(Minecraft.getInstance().player);
        if (!unlocked && isOverLock(mouseX, mouseY)) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(recipe.researchGate().orElseThrow()));
        }
    }

    @Override
    public void draw(RecipeHolder<R> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        R recipe = holder.value();
        boolean unlocked = recipe.doesPassGate(Minecraft.getInstance().player);
        BACKDROP.draw(graphics);
        PLATE.draw(graphics, PLATE_X, PLATE_Y);

        Font font = Minecraft.getInstance().font;
        Optional<Integer> instability = recipe instanceof DynamicInfusionRecipe dynamic ? preview(dynamic).map(InfusionRecipePreview::instability) : Optional.of(recipe.instability());
        instability.ifPresent(value -> {
            Component label = InstabilityLevel.forInstability(value).label();
            graphics.text(font, label, INSTABILITY_CENTER_X - font.width(label) / 2, INSTABILITY_Y, INSTABILITY_COLOR, false);
        });

        if (!unlocked) {
            graphics.item(Items.BARRIER.getDefaultInstance(), LOCK_X, LOCK_Y);
        }
    }

    private static boolean isOverLock(double mouseX, double mouseY) {
        return mouseX > LOCK_X && mouseX < LOCK_X + LOCK_SIZE && mouseY > LOCK_Y && mouseY < LOCK_Y + LOCK_SIZE;
    }

    private static Optional<InfusionRecipePreview> preview(DynamicInfusionRecipe recipe) {
        var level = Minecraft.getInstance().level;
        return level == null ? Optional.empty() : recipe.preview(level.registryAccess());
    }

    private static void addHiddenInputs(IRecipeLayoutBuilder builder, List<AspectInstance> aspects, Function<Holder<IAspect>, ItemStack> stackFactory) {
        for (AspectInstance instance : aspects) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(stackFactory.apply(instance.aspect()));
        }
    }

    private static void addIngredientSlot(IRecipeLayoutBuilder builder, int x, int y, Ingredient ingredient) {
        IRecipeSlotBuilder slot = builder.addInputSlot(x, y);
        if (ingredient.getCustomIngredient() instanceof SpellPartIngredient spellPart) {
            slot.addItemStacks(spellPart.displayStacks());
            return;
        }
        slot.add(ingredient);
    }

    private static void addRingSlots(IRecipeLayoutBuilder builder, List<Ingredient> components) {
        double step = FULL_TURN_DEGREES / components.size();
        int index = 0;
        for (Ingredient component : components) {
            double angle = Math.toRadians(RING_START_DEGREES + index * step);
            int x = CENTER_X + (int) (RING_RADIUS * Math.cos(angle));
            int y = CENTER_Y + (int) (RING_RADIUS * Math.sin(angle));
            addIngredientSlot(builder, x, y, component);
            index++;
        }
    }

    private static void addAspectSlots(IRecipeLayoutBuilder builder, List<AspectInstance> aspects) {
        int left = ASPECT_ROW_OFFSET + ASPECT_X - (aspects.size() * SPACE) / ASPECT_ROW_HALVES;
        int x = left;
        for (AspectInstance instance : aspects) {
            builder.addInputSlot(x, ASPECT_Y).setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE).add(AspectIngredientType.INSTANCE, instance);
            x += SPACE;
        }
    }

    private enum InstabilityLevel {
        STABLE("gui.thaumaturge.infusion.instability.0", ChatFormatting.DARK_BLUE), STEADY("gui.thaumaturge.infusion.instability.1", ChatFormatting.BLUE), UNSETTLED(
                "gui.thaumaturge.infusion.instability.2", ChatFormatting.DARK_PURPLE), VOLATILE("gui.thaumaturge.infusion.instability.3", ChatFormatting.YELLOW), DANGEROUS(
                        "gui.thaumaturge.infusion.instability.4", ChatFormatting.GOLD), CATASTROPHIC("gui.thaumaturge.infusion.instability.5", ChatFormatting.DARK_RED);

        private final String nameKey;
        private final ChatFormatting color;

        InstabilityLevel(String nameKey, ChatFormatting color) {
            this.nameKey = nameKey;
            this.color = color;
        }

        static InstabilityLevel forInstability(int instability) {
            InstabilityLevel[] levels = values();
            return levels[Math.clamp(instability / INSTABILITY_DIVISOR, 0, levels.length - 1)];
        }

        Component label() {
            return Component.translatable(INSTABILITY_KEY, Component.translatable(nameKey).withStyle(color));
        }
    }
}
