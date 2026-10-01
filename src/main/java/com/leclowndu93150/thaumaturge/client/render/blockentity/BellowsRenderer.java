package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockBellows;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockEntityBellows;
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

public final class BellowsRenderer implements BlockEntityRenderer<BlockEntityBellows> {
    private static final int BOTTOM = 0;
    private static final int TOP = 1;
    private static final int BAG = 2;
    public static final ModelResourceLocation[] MODEL_IDS = {
        ModelResourceLocation.standalone(TCIds.rl("block/bellows/bottom_plank")),
        ModelResourceLocation.standalone(TCIds.rl("block/bellows/top_plank")),
        ModelResourceLocation.standalone(TCIds.rl("block/bellows/bag"))
    };

    private final RandomSource random = RandomSource.create();

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityBellows bellows,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BlockState state = bellows.getBlockState();
        Direction facing = state.getValue(BlockBellows.FACING);
        float scale = bellows.inflation;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        switch (facing) {
            case DOWN -> {}
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(270.0F));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(270.0F));
        }
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.translate(0.0F, (scale + 0.1F) * -0.5F, 0.0F);
        poseStack.scale(1.0F, scale + 0.1F, 1.0F);
        renderPart(BAG, state, poseStack, buffers, light, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, (1.0F - scale) * -0.25F, 0.0F);
        renderPart(TOP, state, poseStack, buffers, light, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, (1.0F - scale) * 0.25F, 0.0F);
        renderPart(BOTTOM, state, poseStack, buffers, light, overlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderPart(
            int index, BlockState state, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(MODEL_IDS[index]);
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
