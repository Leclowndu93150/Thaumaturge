package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.TCFlatRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class FloatyLineRenderer {
    private static final ResourceLocation WISPY_TEXTURE = TCIds.rl("textures/misc/wispy.png");

    private static final RenderType WISPY_LINE = TCFlatRenderTypes.entityAdditiveFlat(WISPY_TEXTURE);

    private static final float SEGMENTS_PER_BLOCK = 2.0F;
    private static final float TIME_UNITS_PER_TICK = 50.0F / 30.0F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final float MIN_SPAN = 0.1F;
    private static final float WAVE_AMPLITUDE = 0.5F;
    private static final float WAVE_PERIOD_X = 4.0F;
    private static final float WAVE_PERIOD_Y = 3.0F;
    private static final float WAVE_PERIOD_Z = 2.0F;
    private static final float PHASE_TIME_DIVISOR = 5.0F;
    private static final float END_ATTACH_START = 0.7F;
    private static final float END_ATTACH_ALPHA = 0.8F;

    private FloatyLineRenderer() {}

    public static void draw(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Vec3 fromRelative,
            float time,
            int color,
            float speed,
            float distanceFraction,
            float width) {
        write(
                poseStack.last(),
                buffers.getBuffer(WISPY_LINE),
                fromRelative,
                time,
                color,
                speed,
                distanceFraction,
                width);
    }

    static void write(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            Vec3 fromRelative,
            float time,
            int color,
            float speed,
            float distanceFraction,
            float width) {
        float span = (float) fromRelative.length();
        if (span < MIN_SPAN) {
            return;
        }
        float steps = Math.max(2.0F, Math.round(span) * SEGMENTS_PER_BLOCK);
        int samples = (int) (steps * distanceFraction) + 1;
        float[] xs = new float[samples];
        float[] ys = new float[samples];
        float[] zs = new float[samples];
        float[] us = new float[samples];
        float[] alphas = new float[samples];
        for (int i = 0; i < samples; i++) {
            float t = i / steps;
            float taper = 1.0F - Math.abs(t - 0.5F) * 2.0F;
            float phase = span * (1.0F - t) * SEGMENTS_PER_BLOCK - time / PHASE_TIME_DIVISOR;
            float sway = WAVE_AMPLITUDE * taper;
            xs[i] = ((float) fromRelative.x + Mth.sin(phase / WAVE_PERIOD_X) * sway) * t;
            ys[i] = ((float) fromRelative.y + Mth.sin(phase / WAVE_PERIOD_Y) * sway) * t;
            zs[i] = ((float) fromRelative.z + Mth.sin(phase / WAVE_PERIOD_Z) * sway) * t;
            us[i] = (1.0F - t) * span - time * speed;
            float alpha = Math.max(0.0F, taper);
            if (t > END_ATTACH_START) {
                alpha = Math.max(alpha, (t - END_ATTACH_START) / (1.0F - END_ATTACH_START) * END_ATTACH_ALPHA);
            }
            alphas[i] = alpha;
        }
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        ribbon(pose, buffer, xs, ys, zs, us, alphas, 0.0F, width, red, green, blue);
        ribbon(pose, buffer, xs, ys, zs, us, alphas, width, 0.0F, red, green, blue);
    }

    private static void ribbon(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float[] xs,
            float[] ys,
            float[] zs,
            float[] us,
            float[] alphas,
            float halfX,
            float halfY,
            float red,
            float green,
            float blue) {
        for (int i = 1; i < xs.length; i++) {
            int prev = i - 1;
            int c0 = ARGB32.colorFromFloat(alphas[prev], red, green, blue);
            int c1 = ARGB32.colorFromFloat(alphas[i], red, green, blue);
            edge(pose, buffer, xs[prev] + halfX, ys[prev] - halfY, zs[prev], us[prev], 1.0F, c0);
            edge(pose, buffer, xs[prev] - halfX, ys[prev] + halfY, zs[prev], us[prev], 0.0F, c0);
            edge(pose, buffer, xs[i] - halfX, ys[i] + halfY, zs[i], us[i], 0.0F, c1);
            edge(pose, buffer, xs[i] + halfX, ys[i] - halfY, zs[i], us[i], 1.0F, c1);
        }
    }

    private static void edge(
            PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(EMISSIVE_LIGHT)
                .setNormal(pose, 0, 1, 0);
    }

    public static float time(long gameTime, float partialTicks) {
        return (gameTime + partialTicks) * TIME_UNITS_PER_TICK;
    }
}
