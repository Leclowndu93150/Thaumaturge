package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BeamRenderType;
import com.leclowndu93150.thaumaturge.client.model.entity.ArcaneBoreModel;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityArcaneBore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4fc;

public final class ArcaneBoreRenderer extends MobRenderer<EntityArcaneBore, ArcaneBoreRenderState, ArcaneBoreModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/arcanebore.png");
    private static final Identifier BEAM_TEXTURE = TCIds.rl("textures/misc/beam1.png");
    private static final RenderType BEAM_TYPE = RenderType.create("tc_bore_beam",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", BEAM_TEXTURE).useLightmap().createRenderSetup());

    private static final float SHADOW = 0.5F;
    private static final double BEAM_LENGTH = 5.0;
    private static final float BEAM_RADIUS = 0.15F;
    private static final float BEAM_ALPHA = 0.4F;
    private static final int BEAM_TINT = ARGB.colorFromFloat(BEAM_ALPHA, 0.0F, 1.0F, 0.4F);
    private static final int BEAM_LIGHT = 0x000000C8;
    private static final int BEAM_STRIPS = 3;
    private static final float TIP_FORWARD_OFFSET = 0.5F;
    private static final float TIP_VERTICAL_OFFSET = 0.075F;
    private static final int TIP_FLARE_GRID = 32;
    private static final int TIP_FLARE_FRAME_START = 96;
    private static final int TIP_FLARE_FRAME_COUNT = 32;
    private static final float TIP_FLARE_HALF_SIZE = 0.5F;
    private static final float TIP_FLARE_ALPHA = 0.8F;
    private static final int TIP_FLARE_TINT = ARGB.colorFromFloat(TIP_FLARE_ALPHA, 0.0F, 1.0F, 0.4F);

    public ArcaneBoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ArcaneBoreModel(context.bakeLayer(TCModelLayers.ARCANE_BORE)), SHADOW);
    }

    @Override
    public ArcaneBoreRenderState createRenderState() {
        return new ArcaneBoreRenderState();
    }

    @Override
    public void extractRenderState(EntityArcaneBore entity, ArcaneBoreRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = Mth.wrapDegrees(Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot));
        state.bodyRot = 0.0F;
        state.digging = entity.clientDiggingSmoothed() && entity.isActive() && entity.validInventory();
        state.headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.eyeHeight = entity.getEyeHeight();
        state.beamUvScroll = (entity.tickCount + partialTicks) * 0.2F;
        state.beamSpin = (float) (entity.level().getGameTime() % 72L * 5L) + 5.0F * partialTicks;
        float yaw = state.yRot * Mth.DEG_TO_RAD;
        float pitch = state.headPitch * Mth.DEG_TO_RAD;
        float horizontalOffset = TIP_FORWARD_OFFSET * Mth.cos(pitch) + TIP_VERTICAL_OFFSET * Mth.sin(pitch);
        state.tipX = -Mth.sin(yaw) * horizontalOffset;
        state.tipY = state.eyeHeight + TIP_VERTICAL_OFFSET * Mth.cos(pitch) - TIP_FORWARD_OFFSET * Mth.sin(pitch);
        state.tipZ = Mth.cos(yaw) * horizontalOffset;
        state.tipFrame = TIP_FLARE_FRAME_START + entity.tickCount % TIP_FLARE_FRAME_COUNT;
    }

    @Override
    public void submit(ArcaneBoreRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (!state.digging) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(state.tipX, state.tipY, state.tipZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.mulPose(Axis.XN.rotationDegrees(state.headPitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.beamSpin));
        float uvOffset = state.beamUvScroll - Mth.floor(state.beamUvScroll * 0.5F) * 2.0F;
        for (int strip = 0; strip < BEAM_STRIPS; strip++) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(360.0F / BEAM_STRIPS));
            float v0 = -1.0F + uvOffset + (float) strip / BEAM_STRIPS;
            float v1 = (float) BEAM_LENGTH + v0;
            collector.submitCustomGeometry(poseStack, BEAM_TYPE, (pose, buffer) -> {
                Matrix4fc mat = pose.pose();
                buffer.addVertex(mat, 0.0F, 0.0F, -(float) BEAM_LENGTH).setUv(1.0F, v1).setColor(BEAM_TINT).setLight(BEAM_LIGHT);
                buffer.addVertex(mat, -BEAM_RADIUS, 0.0F, 0.0F).setUv(1.0F, v0).setColor(BEAM_TINT).setLight(BEAM_LIGHT);
                buffer.addVertex(mat, BEAM_RADIUS, 0.0F, 0.0F).setUv(0.0F, v0).setColor(BEAM_TINT).setLight(BEAM_LIGHT);
                buffer.addVertex(mat, 0.0F, 0.0F, -(float) BEAM_LENGTH).setUv(0.0F, v1).setColor(BEAM_TINT).setLight(BEAM_LIGHT);
            });
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(state.tipX, state.tipY, state.tipZ);
        poseStack.mulPose(camera.orientation);
        submitTipFlare(state, poseStack, collector);
        poseStack.popPose();
    }

    private static void submitTipFlare(ArcaneBoreRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float frameSize = 1.0F / TIP_FLARE_GRID;
        float u0 = state.tipFrame % TIP_FLARE_GRID * frameSize;
        float v0 = state.tipFrame / TIP_FLARE_GRID * frameSize;
        float u1 = u0 + frameSize;
        float v1 = v0 + frameSize;
        collector.submitCustomGeometry(poseStack, BeamRenderType.NODE_TYPE, (pose, buffer) -> addTipFlareVertices(buffer, pose.pose(), u0, v0, u1, v1));
    }

    private static void addTipFlareVertices(VertexConsumer buffer, Matrix4fc matrix, float u0, float v0, float u1, float v1) {
        buffer.addVertex(matrix, -TIP_FLARE_HALF_SIZE, -TIP_FLARE_HALF_SIZE, 0.0F).setUv(u1, v1).setColor(TIP_FLARE_TINT);
        buffer.addVertex(matrix, -TIP_FLARE_HALF_SIZE, TIP_FLARE_HALF_SIZE, 0.0F).setUv(u1, v0).setColor(TIP_FLARE_TINT);
        buffer.addVertex(matrix, TIP_FLARE_HALF_SIZE, TIP_FLARE_HALF_SIZE, 0.0F).setUv(u0, v0).setColor(TIP_FLARE_TINT);
        buffer.addVertex(matrix, TIP_FLARE_HALF_SIZE, -TIP_FLARE_HALF_SIZE, 0.0F).setUv(u0, v1).setColor(TIP_FLARE_TINT);
    }

    @Override
    public Identifier getTextureLocation(ArcaneBoreRenderState state) {
        return TEXTURE;
    }
}
