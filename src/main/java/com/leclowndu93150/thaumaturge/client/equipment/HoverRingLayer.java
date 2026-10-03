package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.TCFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.context.ContextKey;

public final class HoverRingLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public static final ContextKey<Boolean> HOVERING = new ContextKey<>(TCIds.rl("hovering"));

    private static final float RING_Y = 0.2F;
    private static final float RING_Z = 0.55F;
    private static final float OUTER_SIZE = 2.5F;
    private static final float INNER_SIZE = 1.5F;
    private static final float INNER_Z = 0.03F;
    private static final float HALF_TURN = 180.0F;
    private static final float HALF = 0.5F;
    private static final int OUTER_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
    private static final int INNER_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.5F, 1.0F);
    private static final float FRAME_HEIGHT = 1.0F / ParticleTextures.LIGHTNING_RING_FRAMES;

    public HoverRingLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible || !state.getRenderDataOrDefault(HOVERING, false)) {
            return;
        }
        RenderType renderType = TCFlatRenderTypes.entityAdditiveFlat(ParticleTextures.LIGHTNING_RING);
        float v0 = Math.floorMod((int) state.ageInTicks, ParticleTextures.LIGHTNING_RING_FRAMES) * FRAME_HEIGHT;
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0F, RING_Y, RING_Z);
        submitQuad(poseStack, collector, renderType, OUTER_SIZE, OUTER_COLOR, v0);
        poseStack.mulPose(Axis.YP.rotationDegrees(HALF_TURN));
        poseStack.translate(0.0F, 0.0F, INNER_Z);
        submitQuad(poseStack, collector, renderType, INNER_SIZE, INNER_COLOR, v0);
        poseStack.popPose();
    }

    private static void submitQuad(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, float size, int color, float v0) {
        float half = size * HALF;
        float v1 = v0 + FRAME_HEIGHT;
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            vertex(buffer, pose, -half, half, 1.0F, v1, color);
            vertex(buffer, pose, half, half, 0.0F, v1, color);
            vertex(buffer, pose, half, -half, 0.0F, v0, color);
            vertex(buffer, pose, -half, -half, 1.0F, v0, color);
        });
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int color) {
        buffer.addVertex(pose.pose(), x, y, 0.0F).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
