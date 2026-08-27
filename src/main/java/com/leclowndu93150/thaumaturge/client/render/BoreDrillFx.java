package com.leclowndu93150.thaumaturge.client.render;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BeamRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class BoreDrillFx {
    private static final double BEAM_LENGTH = 5.0;
    private static final float BEAM_RADIUS = 0.15F;
    private static final float BEAM_ALPHA = 0.4F;
    private static final int BEAM_TINT = ARGB32.colorFromFloat(BEAM_ALPHA, 0.0F, 1.0F, 0.4F);
    private static final RenderType BEAM_TYPE = TCRenderTypes.fxAdditive(TCIds.rl("textures/misc/beam1.png"));
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
    private static final float BEAM_SCROLL_SPEED = 0.2F;
    private static final long BEAM_SPIN_PERIOD = 72L;
    private static final float BEAM_SPIN_STEP = 5.0F;

    private BoreDrillFx() {}

    public static float beamUvScroll(float ticks) {
        return ticks * BEAM_SCROLL_SPEED;
    }

    public static float beamSpin(long gameTime, float partialTicks) {
        return gameTime % BEAM_SPIN_PERIOD * BEAM_SPIN_STEP + BEAM_SPIN_STEP * partialTicks;
    }

    public static int tipFrame(int ticks) {
        return TIP_FLARE_FRAME_START + ticks % TIP_FLARE_FRAME_COUNT;
    }

    public static Vec3 tipOffset(float yawDegrees, float pitchDegrees, float eyeHeight) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        float pitch = pitchDegrees * Mth.DEG_TO_RAD;
        float horizontal = TIP_FORWARD_OFFSET * Mth.cos(pitch) + TIP_VERTICAL_OFFSET * Mth.sin(pitch);
        return new Vec3(
                -Mth.sin(yaw) * horizontal,
                eyeHeight + TIP_VERTICAL_OFFSET * Mth.cos(pitch) - TIP_FORWARD_OFFSET * Mth.sin(pitch),
                Mth.cos(yaw) * horizontal);
    }

    public static void render(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Quaternionf cameraOrientation,
            Vec3 tip,
            float yawDegrees,
            float pitchDegrees,
            float beamUvScroll,
            float beamSpin,
            int tipFrame) {
        poseStack.pushPose();
        poseStack.translate(tip.x, tip.y, tip.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yawDegrees));
        poseStack.mulPose(Axis.XN.rotationDegrees(pitchDegrees));
        poseStack.mulPose(Axis.ZP.rotationDegrees(beamSpin));
        float uvOffset = beamUvScroll - Mth.floor(beamUvScroll * 0.5F) * 2.0F;
        for (int strip = 0; strip < BEAM_STRIPS; strip++) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(360.0F / BEAM_STRIPS));
            float v0 = -1.0F + uvOffset + (float) strip / BEAM_STRIPS;
            float v1 = (float) BEAM_LENGTH + v0;
            addBeamVertices(buffers.getBuffer(BEAM_TYPE), poseStack.last().pose(), v0, v1);
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(tip.x, tip.y, tip.z);
        poseStack.mulPose(cameraOrientation);
        renderTipFlare(tipFrame, poseStack, buffers);
        poseStack.popPose();
    }

    private static void addBeamVertices(VertexConsumer buffer, Matrix4f matrix, float v0, float v1) {
        buffer.addVertex(matrix, 0.0F, 0.0F, -(float) BEAM_LENGTH)
                .setUv(1.0F, v1)
                .setColor(BEAM_TINT)
                .setLight(BEAM_LIGHT);
        buffer.addVertex(matrix, -BEAM_RADIUS, 0.0F, 0.0F)
                .setUv(1.0F, v0)
                .setColor(BEAM_TINT)
                .setLight(BEAM_LIGHT);
        buffer.addVertex(matrix, BEAM_RADIUS, 0.0F, 0.0F)
                .setUv(0.0F, v0)
                .setColor(BEAM_TINT)
                .setLight(BEAM_LIGHT);
        buffer.addVertex(matrix, 0.0F, 0.0F, -(float) BEAM_LENGTH)
                .setUv(0.0F, v1)
                .setColor(BEAM_TINT)
                .setLight(BEAM_LIGHT);
    }

    private static void renderTipFlare(int tipFrame, PoseStack poseStack, MultiBufferSource buffers) {
        float frameSize = 1.0F / TIP_FLARE_GRID;
        float u0 = tipFrame % TIP_FLARE_GRID * frameSize;
        float v0 = tipFrame / TIP_FLARE_GRID * frameSize;
        float u1 = u0 + frameSize;
        float v1 = v0 + frameSize;
        addTipFlareVertices(
                buffers.getBuffer(BeamRenderType.NODE_TYPE), poseStack.last().pose(), u0, v0, u1, v1);
    }

    private static void addTipFlareVertices(
            VertexConsumer buffer, Matrix4f matrix, float u0, float v0, float u1, float v1) {
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
}
