package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class HoverRingLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float RING_Y = 0.2F;
    private static final float RING_Z = 0.55F;
    private static final float OUTER_SIZE = 2.5F;
    private static final float INNER_SIZE = 1.5F;
    private static final float INNER_Z = 0.03F;
    private static final float HALF_TURN = 180.0F;
    private static final float HALF = 0.5F;
    private static final int OUTER_COLOR = 0xFFFFFFFF;
    private static final int INNER_COLOR = 0xFFFF7FFF;
    private static final float FRAME_HEIGHT = 1.0F / ParticleTextures.LIGHTNING_RING_FRAMES;

    public HoverRingLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource collector,
            int lightCoords,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float yRot,
            float xRot) {
        if (player.isInvisible() || !HoverManager.isHovering(player) || !HoverManager.isWearingHoverGear(player)) {
            return;
        }
        RenderType renderType = TTFlatRenderTypes.entityAdditiveFlat(ParticleTextures.LIGHTNING_RING);
        float v0 = Math.floorMod((int) ageInTicks, ParticleTextures.LIGHTNING_RING_FRAMES) * FRAME_HEIGHT;
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0F, RING_Y, RING_Z);
        submitQuad(poseStack, collector, renderType, OUTER_SIZE, OUTER_COLOR, v0);
        poseStack.mulPose(Axis.YP.rotationDegrees(HALF_TURN));
        poseStack.translate(0.0F, 0.0F, INNER_Z);
        submitQuad(poseStack, collector, renderType, INNER_SIZE, INNER_COLOR, v0);
        poseStack.popPose();
    }

    private static void submitQuad(
            PoseStack poseStack, MultiBufferSource collector, RenderType renderType, float size, int color, float v0) {
        float half = size * HALF;
        float v1 = v0 + FRAME_HEIGHT;
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = collector.getBuffer(renderType);
        vertex(buffer, pose, -half, half, 1.0F, v1, color);
        vertex(buffer, pose, half, half, 0.0F, v1, color);
        vertex(buffer, pose, half, -half, 0.0F, v0, color);
        vertex(buffer, pose, -half, -half, 1.0F, v0, color);
    }

    private static void vertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int color) {
        buffer.addVertex(pose.pose(), x, y, 0.0F)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
