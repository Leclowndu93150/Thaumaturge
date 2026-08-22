package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BeamRenderType;
import com.leclowndu93150.thaumaturge.client.model.entity.ArcaneBoreModel;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityArcaneBore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class ArcaneBoreRenderer extends MobRenderer<EntityArcaneBore, ArcaneBoreModel> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/arcanebore.png");
    private static final ResourceLocation BEAM_TEXTURE = TCIds.rl("textures/misc/beam1.png");
    private static final RenderType BEAM_TYPE = TCRenderTypes.fxAdditive(BEAM_TEXTURE);

    private static final float SHADOW = 0.5F;
    private static final double BEAM_LENGTH = 5.0;
    private static final float BEAM_RADIUS = 0.15F;
    private static final float BEAM_ALPHA = 0.4F;
    private static final int BEAM_TINT = ARGB32.colorFromFloat(BEAM_ALPHA, 0.0F, 1.0F, 0.4F);
    private static final int BEAM_LIGHT = 0x000000C8;
    private static final int BEAM_STRIPS = 3;
    private static final float TIP_FORWARD_OFFSET = 0.5F;
    private static final float TIP_VERTICAL_OFFSET = 0.075F;
    private static final int TIP_FLARE_GRID = 32;
    private static final int TIP_FLARE_FRAME_START = 96;
    private static final int TIP_FLARE_FRAME_COUNT = 32;
    private static final float TIP_FLARE_HALF_SIZE = 0.5F;
    private static final float TIP_FLARE_ALPHA = 0.8F;
    private static final int TIP_FLARE_TINT = ARGB32.colorFromFloat(TIP_FLARE_ALPHA, 0.0F, 1.0F, 0.4F);

    public ArcaneBoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ArcaneBoreModel(context.bakeLayer(TCModelLayers.ARCANE_BORE)), SHADOW);
    }

    @Override
    public void render(
            EntityArcaneBore entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        if (!(entity.clientDiggingSmoothed() && entity.isActive() && entity.validInventory())) {
            return;
        }
        float headYaw = Mth.wrapDegrees(Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot));
        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float eyeHeight = entity.getEyeHeight();
        float beamUvScroll = (entity.tickCount + partialTicks) * 0.2F;
        float beamSpin = (float) (entity.level().getGameTime() % 72L * 5L) + 5.0F * partialTicks;
        float yaw = headYaw * Mth.DEG_TO_RAD;
        float pitch = headPitch * Mth.DEG_TO_RAD;
        float horizontalOffset = TIP_FORWARD_OFFSET * Mth.cos(pitch) + TIP_VERTICAL_OFFSET * Mth.sin(pitch);
        float tipX = -Mth.sin(yaw) * horizontalOffset;
        float tipY = eyeHeight + TIP_VERTICAL_OFFSET * Mth.cos(pitch) - TIP_FORWARD_OFFSET * Mth.sin(pitch);
        float tipZ = Mth.cos(yaw) * horizontalOffset;
        poseStack.pushPose();
        poseStack.translate(tipX, tipY, tipZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - headYaw));
        poseStack.mulPose(Axis.XN.rotationDegrees(headPitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(beamSpin));
        float uvOffset = beamUvScroll - Mth.floor(beamUvScroll * 0.5F) * 2.0F;
        VertexConsumer buffer = buffers.getBuffer(BEAM_TYPE);
        for (int strip = 0; strip < BEAM_STRIPS; strip++) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(360.0F / BEAM_STRIPS));
            float v0 = -1.0F + uvOffset + (float) strip / BEAM_STRIPS;
            float v1 = (float) BEAM_LENGTH + v0;
            Matrix4f mat = poseStack.last().pose();
            buffer.addVertex(mat, 0.0F, 0.0F, -(float) BEAM_LENGTH)
                    .setUv(1.0F, v1)
                    .setColor(BEAM_TINT)
                    .setLight(BEAM_LIGHT);
            buffer.addVertex(mat, -BEAM_RADIUS, 0.0F, 0.0F)
                    .setUv(1.0F, v0)
                    .setColor(BEAM_TINT)
                    .setLight(BEAM_LIGHT);
            buffer.addVertex(mat, BEAM_RADIUS, 0.0F, 0.0F)
                    .setUv(0.0F, v0)
                    .setColor(BEAM_TINT)
                    .setLight(BEAM_LIGHT);
            buffer.addVertex(mat, 0.0F, 0.0F, -(float) BEAM_LENGTH)
                    .setUv(0.0F, v1)
                    .setColor(BEAM_TINT)
                    .setLight(BEAM_LIGHT);
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(tipX, tipY, tipZ);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        renderTipFlare(entity, poseStack, buffers);
        poseStack.popPose();
    }

    private static void renderTipFlare(EntityArcaneBore entity, PoseStack poseStack, MultiBufferSource buffers) {
        int frame = TIP_FLARE_FRAME_START + entity.tickCount % TIP_FLARE_FRAME_COUNT;
        float frameSize = 1.0F / TIP_FLARE_GRID;
        float u0 = frame % TIP_FLARE_GRID * frameSize;
        float v0 = frame / TIP_FLARE_GRID * frameSize;
        float u1 = u0 + frameSize;
        float v1 = v0 + frameSize;
        VertexConsumer buffer = buffers.getBuffer(BeamRenderType.NODE_TYPE);
        Matrix4f matrix = poseStack.last().pose();
        buffer.addVertex(matrix, -TIP_FLARE_HALF_SIZE, -TIP_FLARE_HALF_SIZE, 0.0F)
                .setUv(u1, v1)
                .setColor(TIP_FLARE_TINT);
        buffer.addVertex(matrix, -TIP_FLARE_HALF_SIZE, TIP_FLARE_HALF_SIZE, 0.0F)
                .setUv(u1, v0)
                .setColor(TIP_FLARE_TINT);
        buffer.addVertex(matrix, TIP_FLARE_HALF_SIZE, TIP_FLARE_HALF_SIZE, 0.0F)
                .setUv(u0, v0)
                .setColor(TIP_FLARE_TINT);
        buffer.addVertex(matrix, TIP_FLARE_HALF_SIZE, -TIP_FLARE_HALF_SIZE, 0.0F)
                .setUv(u0, v1)
                .setColor(TIP_FLARE_TINT);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityArcaneBore entity) {
        return TEXTURE;
    }
}
