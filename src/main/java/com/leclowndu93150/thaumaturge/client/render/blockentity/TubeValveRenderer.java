package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeValve;
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
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class TubeValveRenderer implements BlockEntityRenderer<BlockEntityTubeValve> {
    public static final ModelResourceLocation MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/tube_valve_head"));

    private final RandomSource random = RandomSource.create();

    public TubeValveRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityTubeValve valve,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        orientTo(valve.facing(), poseStack);
        float rotation = valve.rotation(partialTick);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation * 1.5F));
        poseStack.translate(0.0F, -0.03F - rotation / 360.0F * 0.09F, 0.0F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        renderModel(valve.getBlockState(), poseStack, buffers, light, overlay);
        poseStack.popPose();
    }

    private static void orientTo(Direction direction, PoseStack poseStack) {
        switch (direction) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
            case UP -> {}
        }
    }

    private void renderModel(BlockState state, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(MODEL_ID);
        ModelBlockRenderer modelRenderer =
                Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            modelRenderer.renderModel(
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
}
