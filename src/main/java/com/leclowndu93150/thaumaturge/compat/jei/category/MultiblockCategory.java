package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.recipe.*;
import com.leclowndu93150.thaumaturge.client.screen.pip.BlockPreviewRenderState;
import com.leclowndu93150.thaumaturge.compat.jei.ThaumaturgeJEIPlugin;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.mixin.client.gui.GuiGraphicsExtractorAccessor;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.*;
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
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public final class MultiblockCategory implements IRecipeCategory<RecipeHolder<DustTrigger>> {
    public static final IRecipeHolderType<DustTrigger> RECIPE_TYPE = IRecipeHolderType.create(Identifier.fromNamespaceAndPath(TCIds.MODID, "multiblock_dust_trigger"));

    private static final int WIDTH = 144;
    private static final int HEIGHT = 108;

    private static final IDrawable resultIcon = new AlphaDrawable(Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"), 41, 7, 30, 30);
    private static final IDrawable arrow = new AlphaDrawable(Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"), 199, 168, 26, 26);
    private final IDrawable icon;

    private static final int DUST_SLOT_X = WIDTH / 2 - arrow.getWidth() / 2 - 20 - 18;
    private static final int DUST_SLOT_Y = -3;
    private static final int RESULT_SLOT_X = 118;
    private static final int RESULT_SLOT_Y = HEIGHT / 2 - 9;

    private int rotation = 0;

    public MultiblockCategory(IGuiHelper guiHelper) {
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
        return Component.translatable("jei.thaumaturge.category.multiblock_dust_trigger");
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

        Component usage = Component.translatable("jei.thaumaturge.dust_trigger.target.multiblock");
        builder.addSlot(RecipeIngredientRole.INPUT, DUST_SLOT_X + 1, DUST_SLOT_Y + 1).add(TCItems.SALIS_MUNDUS.get()).addRichTooltipCallback((view, tooltip) -> tooltip.add(usage));

        DustTrigger recipe = holder.value();

        ItemStack result = DustTriggerCategory.resultStack(recipe);
        if (!result.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_SLOT_X + 1, RESULT_SLOT_Y + 1).add(result);
        }

        Object2IntMap<BlueprintSource> inputMap = new Object2IntOpenHashMap<>();
        Blueprint blueprint = lookupBlueprint(((DustTriggerMultiblockRecipe) recipe).blueprintId());
        if (blueprint != null) {
            for (int y = 0; y < blueprint.ySize(); y++) {
                for (int x = 0; x < blueprint.xSize(); x++) {
                    for (int z = 0; z < blueprint.zSize(); z++) {
                        BlueprintPart part = blueprint.cell(y, x, z);
                        if (part != null && !part.source().getRepresentations().isEmpty()) {
                            inputMap.computeInt(part.source(), (key, val) -> val == null ? 1 : val + 1);
                        }
                    }
                }
            }
        }
        int index = 0;
        for (Object2IntMap.Entry<BlueprintSource> entry : inputMap.object2IntEntrySet().stream().sorted(Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).toList().reversed()) {
            List<ItemStack> stacks = entry.getKey().getRepresentations();
            int count = entry.getIntValue();
            builder.addInputSlot(5 + index * 20, HEIGHT - 20).addItemStacks(stacks.stream().map(stack -> stack.copyWithCount(count)).toList());
            index++;
        }
    }

    @Override
    public void draw(RecipeHolder<DustTrigger> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        resultIcon.draw(guiGraphics, RESULT_SLOT_X - 6, RESULT_SLOT_Y - 6);
        arrow.draw(guiGraphics, WIDTH / 2 - arrow.getWidth() / 2 - 20, 0);
        boolean doesPassGate = holder.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate)
            guiGraphics.item(Items.BARRIER.getDefaultInstance(), WIDTH / 2 - arrow.getWidth() / 2 - 14, 4);
        Matrix3x2f pose = guiGraphics.pose();
        drawExtra(holder, new Rect2i(Math.round(pose.m20), Math.round(pose.m21), WIDTH, HEIGHT), guiGraphics, mouseX, mouseY);
    }

    private void drawExtra(RecipeHolder<DustTrigger> holder, Rect2i area, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        DustTriggerMultiblockRecipe recipe = (DustTriggerMultiblockRecipe) holder.value();
        Blueprint blueprint = lookupBlueprint(recipe.blueprintId());
        if (blueprint != null) {
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
            ((GuiGraphicsExtractorAccessor) guiGraphics).thaumaturge$getGuiRenderState().addPicturesInPictureState(
                    new BlockPreviewRenderState(blocks, 25, rotation / 8F + 90, 1, 15, 0, 0, area.getX() - 35, area.getY() + 5, area.getX() + WIDTH, area.getY() + HEIGHT, null));
            rotation++;
        }
    }

    private @Nullable Blueprint lookupBlueprint(Identifier blueprintId) {
        ResourceKey<Blueprint> key = ResourceKey.create(Blueprint.REGISTRY_KEY, blueprintId);
        if (ThaumaturgeJEIPlugin.clientRegistryAccess() == null)
            return null;
        Registry<Blueprint> registry = ThaumaturgeJEIPlugin.clientRegistryAccess().lookup(Blueprint.REGISTRY_KEY).orElse(null);
        if (registry == null) {
            return null;
        }
        Holder<Blueprint> holder = registry.get(key).orElse(null);
        return holder == null ? null : holder.value();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<DustTrigger> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        Optional<ResearchGate> gate = recipe.value().researchGate();
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate && mouseX > WIDTH / 2 - arrow.getWidth() / 2 - 14 && mouseX < WIDTH / 2 - arrow.getWidth() / 2 + 4 && mouseY > 4 && mouseY < 20) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(gate.get()));
        }
    }
}
