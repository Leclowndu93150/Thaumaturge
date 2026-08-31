package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Renders the original TC4 Advanced Alchemical Furnace model around its modern controller. */
public final class AdvancedAlchemicalFurnaceRenderer
        implements BlockEntityRenderer<BlockEntityAdvancedAlchemicalFurnace> {
    public static final ModelResourceLocation BASE_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/advanced_alchemical_furnace_base"));
    public static final ModelResourceLocation BASE_ON_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/advanced_alchemical_furnace_base_on"));
    public static final ModelResourceLocation TANK_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/advanced_alchemical_furnace_tank"));
    public static final ModelResourceLocation TANK_ON_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/advanced_alchemical_furnace_tank_on"));

    private final RandomSource random = RandomSource.create();

    public AdvancedAlchemicalFurnaceRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityAdvancedAlchemicalFurnace furnace,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (!furnace.assembled()) {
            return;
        }

        BlockState state = furnace.getBlockState();
        boolean hot = furnace.heat() > 100;
        boolean charged = !furnace.aspects().isEmpty();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        renderModel(state, hot ? BASE_ON_MODEL_ID : BASE_MODEL_ID, poseStack, buffers, light, overlay);
        ModelResourceLocation tank = charged ? TANK_ON_MODEL_ID : TANK_MODEL_ID;
        for (int rotation = 0; rotation < 4; rotation++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * rotation));
            renderModel(state, tank, poseStack, buffers, light, overlay);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderModel(
            BlockState state,
            ModelResourceLocation modelId,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelId);
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            renderer.renderModel(
                    poseStack.last(),
                    buffers.getBuffer(renderType),
                    state,
                    model,
                    1.0F,
                    1.0F,
                    1.0F,
                    light,
                    overlay,
                    ModelData.EMPTY,
                    renderType);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityAdvancedAlchemicalFurnace furnace) {
        return true;
    }
}
