package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.recipe.*;
import com.leclowndu93150.thaumaturge.compat.jei.ThaumaturgeJEIPlugin;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class MultiblockCategory implements IRecipeCategory<RecipeHolder<DustTrigger>> {
    public static final RecipeType<RecipeHolder<DustTrigger>> RECIPE_TYPE = RecipeType.createRecipeHolderType(
            ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "multiblock_dust_trigger"));

    private static final int WIDTH = 144;
    private static final int HEIGHT = 108;

    private static final IDrawable resultIcon = new AlphaDrawable(
            ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"),
            41,
            7,
            30,
            30);
    private static final IDrawable arrow = new AlphaDrawable(
            ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"),
            199,
            168,
            26,
            26);
    private final IDrawable icon;

    private static final int DUST_SLOT_X = WIDTH / 2 - arrow.getWidth() / 2 - 20 - 18;
    private static final int DUST_SLOT_Y = -3;
    private static final int RESULT_SLOT_X = 118;
    private static final int RESULT_SLOT_Y = HEIGHT / 2 - 9;

    private final BlockEntityInfusionMatrix matrixPreview = new BlockEntityInfusionMatrix(
            BlockPos.ZERO, TCBlocks.INFUSION_MATRIX.get().defaultBlockState());
    private int rotation = 0;

    public MultiblockCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TCItems.SALIS_MUNDUS.get()));
    }

    @Override
    public RecipeType<RecipeHolder<DustTrigger>> getRecipeType() {
        return RECIPE_TYPE;
    }

    public static RecipeType<RecipeHolder<DustTrigger>> type() {
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
        builder.addSlot(RecipeIngredientRole.INPUT, DUST_SLOT_X + 1, DUST_SLOT_Y + 1)
                .addItemStack(new ItemStack(TCItems.SALIS_MUNDUS.get()))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(usage));

        DustTrigger recipe = holder.value();
        Blueprint blueprint = lookupBlueprint(((DustTriggerMultiblockRecipe) recipe).blueprintId());

        ItemStack result = DustTriggerCategory.resultStack(recipe);
        if (!result.isEmpty() && !isKeptBlueprintInput(blueprint, result)) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_SLOT_X + 1, RESULT_SLOT_Y + 1)
                    .addItemStack(result);
        }

        Object2IntMap<BlueprintSource> inputMap = new Object2IntOpenHashMap<>();
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
        for (Object2IntMap.Entry<BlueprintSource> entry : inputMap.object2IntEntrySet().stream()
                .sorted(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
                .toList()
                .reversed()) {
            List<ItemStack> stacks = entry.getKey().getRepresentations();
            int count = entry.getIntValue();
            builder.addInputSlot(5 + index * 20, HEIGHT - 20)
                    .addItemStacks(stacks.stream()
                            .map(stack -> stack.copyWithCount(count))
                            .toList());
            index++;
        }
    }

    private static final float PREVIEW_CENTER_X = (WIDTH - 35) / 2.0F;
    private static final float PREVIEW_CENTER_Y = (HEIGHT + 5) / 2.0F;
    private static final float PREVIEW_SCALE = 15.0F;
    private static final float PREVIEW_ROT_X = 25.0F;
    private static final float PREVIEW_DEPTH = 200.0F;

    @Override
    public void draw(
            RecipeHolder<DustTrigger> holder,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY) {
        Blueprint blueprint = lookupBlueprint(((DustTriggerMultiblockRecipe) holder.value()).blueprintId());
        ItemStack result = DustTriggerCategory.resultStack(holder.value());
        if (!result.isEmpty() && !isKeptBlueprintInput(blueprint, result)) {
            resultIcon.draw(guiGraphics, RESULT_SLOT_X - 6, RESULT_SLOT_Y - 6);
        }
        arrow.draw(guiGraphics, WIDTH / 2 - arrow.getWidth() / 2 - 20, 0);
        boolean doesPassGate = holder.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate)
            guiGraphics.renderItem(Items.BARRIER.getDefaultInstance(), WIDTH / 2 - arrow.getWidth() / 2 - 14, 4);
        drawExtra(holder, guiGraphics);
    }

    private void drawExtra(RecipeHolder<DustTrigger> holder, GuiGraphics guiGraphics) {
        DustTriggerMultiblockRecipe recipe = (DustTriggerMultiblockRecipe) holder.value();
        Blueprint blueprint = lookupBlueprint(recipe.blueprintId());
        if (blueprint == null) {
            return;
        }
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int y = 0; y < blueprint.ySize(); y++) {
            for (int x = 0; x < blueprint.xSize(); x++) {
                for (int z = 0; z < blueprint.zSize(); z++) {
                    BlueprintPart part = blueprint.cell(y, x, z);
                    if (part != null) {
                        int py = -y + (blueprint.ySize() - 1);
                        blocks.put(new BlockPos(x, py, z), part.source().getState());
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, py);
                        maxY = Math.max(maxY, py);
                        minZ = Math.min(minZ, z);
                        maxZ = Math.max(maxZ, z);
                    }
                }
            }
        }
        if (blocks.isEmpty()) {
            return;
        }
        float centerX = (minX + maxX + 1) / 2.0F;
        float centerY = (minY + maxY + 1) / 2.0F;
        float centerZ = (minZ + maxZ + 1) / 2.0F;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        MultiBufferSource.BufferSource buffers = guiGraphics.bufferSource();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(PREVIEW_CENTER_X, PREVIEW_CENTER_Y, PREVIEW_DEPTH);
        pose.scale(PREVIEW_SCALE, -PREVIEW_SCALE, PREVIEW_SCALE);
        pose.mulPose(Axis.XP.rotationDegrees(PREVIEW_ROT_X));
        pose.mulPose(Axis.YP.rotationDegrees(rotation / 8F + 90));
        pose.translate(-centerX, -centerY, -centerZ);
        Lighting.setupFor3DItems();
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos blockPos = entry.getKey();
            pose.pushPose();
            pose.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            BlockState state = entry.getValue();
            if (state.is(TCBlocks.INFUSION_MATRIX.get())) {
                Minecraft.getInstance()
                        .getBlockEntityRenderDispatcher()
                        .renderItem(matrixPreview, pose, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            } else {
                dispatcher.renderSingleBlock(state, pose, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }
            pose.popPose();
        }
        buffers.endBatch();
        Lighting.setupForFlatItems();
        pose.popPose();
        rotation++;
    }

    private static boolean isKeptBlueprintInput(@Nullable Blueprint blueprint, ItemStack stack) {
        if (blueprint == null || stack.isEmpty()) {
            return false;
        }
        for (int y = 0; y < blueprint.ySize(); y++) {
            for (int x = 0; x < blueprint.xSize(); x++) {
                for (int z = 0; z < blueprint.zSize(); z++) {
                    BlueprintPart part = blueprint.cell(y, x, z);
                    if (part == null || !(part.target() instanceof BlueprintTarget.Keep)) {
                        continue;
                    }
                    for (ItemStack representation : part.source().getRepresentations()) {
                        if (ItemStack.isSameItemSameComponents(stack, representation)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private @Nullable Blueprint lookupBlueprint(ResourceLocation blueprintId) {
        ResourceKey<Blueprint> key = ResourceKey.create(Blueprint.REGISTRY_KEY, blueprintId);
        RegistryAccess access = ThaumaturgeJEIPlugin.clientRegistryAccess();
        if (access == null) {
            return null;
        }
        return access.lookup(Blueprint.REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(key))
                .map(Holder::value)
                .orElse(null);
    }

    @Override
    public void getTooltip(
            ITooltipBuilder tooltip,
            RecipeHolder<DustTrigger> recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY) {
        Optional<ResearchGate> gate = recipe.value().researchGate();
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate
                && mouseX > WIDTH / 2 - arrow.getWidth() / 2 - 14
                && mouseX < WIDTH / 2 - arrow.getWidth() / 2 + 4
                && mouseY > 4
                && mouseY < 20) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(gate.get()));
        }
    }
}
