package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirrorBase;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockMirror;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class MirrorRenderer implements BlockEntityRenderer<BlockEntityMirrorBase> {
    public static final ModelResourceLocation FRAME_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/mirror"));
    public static final ModelResourceLocation FRAME_ESSENTIA_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/mirror_essentia"));

    private static final ResourceLocation PANE_TEXTURE = TCIds.rl("textures/block/mirrorpane.png");
    private static final ResourceLocation PANE_TRANS_TEXTURE = TCIds.rl("textures/block/mirrorpanetrans.png");

    private static final int[][] WINDOW_ROWS = {
        {2, 5, 11},
        {3, 5, 11},
        {4, 4, 12},
        {5, 3, 13},
        {6, 3, 13},
        {7, 3, 13},
        {8, 3, 13},
        {9, 3, 13},
        {10, 3, 13},
        {11, 4, 12},
        {12, 5, 11},
        {13, 5, 11}
    };
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float PORTAL_PLANE = -0.5F + 0.018F;
    private static final float PANE_PLANE = -0.5F + 0.02F;
    private static final float FRAME_CENTER_LIFT = 0.46875F;
    private static final double PORTAL_RANGE_SQ = 1024.0;

    private final RandomSource random = RandomSource.create();

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityMirrorBase mirror,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BlockState state = mirror.getBlockState();
        Direction facing = state.getValue(BlockMirror.FACING);
        boolean linked = mirror.linked;
        var player = Minecraft.getInstance().player;
        boolean inRange =
                player != null && player.distanceToSqr(Vec3.atCenterOf(mirror.getBlockPos())) < PORTAL_RANGE_SQ;
        boolean essentia = state.getBlock() instanceof BlockMirror mirrorBlock && mirrorBlock.isEssentia();

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        float xRot = facing == Direction.UP ? 0.0F : facing == Direction.DOWN ? 180.0F : 90.0F;
        float yRot =
                switch (facing) {
                    case EAST -> 90.0F;
                    case SOUTH -> 180.0F;
                    case WEST -> 270.0F;
                    default -> 0.0F;
                };
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-xRot));

        poseStack.pushPose();
        poseStack.translate(0.0F, -FRAME_CENTER_LIFT, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        renderFrame(essentia ? FRAME_ESSENTIA_MODEL_ID : FRAME_MODEL_ID, state, poseStack, buffers, light, overlay);
        poseStack.popPose();

        if (linked && inRange) {
            VertexConsumer surface = buffers.getBuffer(EldritchPortalSurface.surface());
            portalWindow(poseStack.last(), surface, mirror.getBlockPos());
        }
        RenderType paneType = RenderType.entityTranslucent(linked && inRange ? PANE_TRANS_TEXTURE : PANE_TEXTURE);
        paneQuad(poseStack.last(), buffers.getBuffer(paneType), light);
        poseStack.popPose();
    }

    private void renderFrame(
            ModelResourceLocation id,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(id);
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

    private static void portalWindow(PoseStack.Pose pose, VertexConsumer buffer, BlockPos worldPos) {
        for (int[] row : WINDOW_ROWS) {
            float z0 = row[0] * PIXEL - 0.5F;
            float z1 = z0 + PIXEL;
            float x0 = row[1] * PIXEL - 0.5F;
            float x1 = row[2] * PIXEL - 0.5F;
            EldritchPortalSurface.quad(
                    pose,
                    buffer,
                    worldPos,
                    x0,
                    PORTAL_PLANE,
                    z0,
                    x0,
                    PORTAL_PLANE,
                    z1,
                    x1,
                    PORTAL_PLANE,
                    z1,
                    x1,
                    PORTAL_PLANE,
                    z0);
        }
    }

    private static void paneQuad(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        paneVertex(buffer, pose, -0.5F, -0.5F, 0.0F, 0.0F, light);
        paneVertex(buffer, pose, -0.5F, 0.5F, 0.0F, 1.0F, light);
        paneVertex(buffer, pose, 0.5F, 0.5F, 1.0F, 1.0F, light);
        paneVertex(buffer, pose, 0.5F, -0.5F, 1.0F, 0.0F, light);
    }

    private static void paneVertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float z, float u, float v, int light) {
        buffer.addVertex(pose, x, PANE_PLANE, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
