package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityCentrifuge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class CentrifugeRenderer implements BlockEntityRenderer<BlockEntityCentrifuge> {
    private static final float BLOCK_CENTER = 0.5F;

    public static final ModelResourceLocation SPINNER_MODEL_ID =
            ModelResourceLocation.standalone(TTIds.rl("block/centrifuge_spinner"));

    private final RandomSource random = RandomSource.create();

    public CentrifugeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityCentrifuge centrifuge,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        float rotation = centrifuge.rotation + centrifuge.rotationSpeed * partialTick;
        poseStack.pushPose();
        poseStack.translate(BLOCK_CENTER, BLOCK_CENTER, BLOCK_CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-BLOCK_CENTER, -BLOCK_CENTER, -BLOCK_CENTER);
        var minecraft = Minecraft.getInstance();
        var model = minecraft.getModelManager().getModel(SPINNER_MODEL_ID);
        var state = centrifuge.getBlockState();
        for (RenderType type : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            minecraft
                    .getBlockRenderer()
                    .getModelRenderer()
                    .renderModel(
                            poseStack.last(),
                            buffers.getBuffer(RenderTypeHelper.getEntityRenderType(type, false)),
                            state,
                            model,
                            1.0F,
                            1.0F,
                            1.0F,
                            light,
                            overlay,
                            ModelData.EMPTY,
                            type);
        }
        poseStack.popPose();
    }
}
