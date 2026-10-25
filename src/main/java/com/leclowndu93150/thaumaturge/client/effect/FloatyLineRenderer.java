package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
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
    private static final ResourceLocation WISPY_TEXTURE = TTIds.rl("textures/misc/wispy.png");

    private static final RenderType WISPY_LINE = TTFlatRenderTypes.entityAdditiveFlat(WISPY_TEXTURE);

    private static final float SEGMENTS_PER_BLOCK = 3.0F;
    private static final long CLOCK_WRAP = 1_048_576L;
    private static final float TIME_UNITS_PER_TICK = 50.0F / 30.0F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final float MIN_SPAN = 0.1F;

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
        float progress = Mth.clamp(distanceFraction, 0.0F, 1.0F);
        if (span < MIN_SPAN || progress <= 0.0F) {
            return;
        }
        Vec3 direction = fromRelative.scale(1.0 / span);
        Vec3 reference = Math.abs(direction.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = direction.cross(reference).normalize();
        Vec3 up = direction.cross(side);
        Vec3 sideWidth = side.scale(width);
        Vec3 upWidth = up.scale(width);
        int segments = Math.max(2, Mth.ceil(span * SEGMENTS_PER_BLOCK));
        int visibleSegments = Mth.ceil(segments * progress);
        float radius = Math.min(0.36F, span * 0.08F);
        Vec3 previous = Vec3.ZERO;
        float previousU = span - time * speed;
        int previousTint = ARGB32.color(0, color);
        for (int sample = 1; sample <= visibleSegments; sample++) {
            float t = Math.min(progress, sample / (float) segments);
            float envelope = 4.0F * t * (1.0F - t);
            float phase = t * Mth.TWO_PI * 1.4F - time * 0.17F;
            float across = Mth.cos(phase) * radius * envelope;
            float lift = Mth.sin(phase) * radius * envelope;
            Vec3 point = new Vec3(
                    fromRelative.x * t + side.x * across + up.x * lift,
                    fromRelative.y * t + side.y * across + up.y * lift,
                    fromRelative.z * t + side.z * across + up.z * lift);
            float u = span * (1.0F - t) - time * speed;
            float opacity = Math.min(1.0F, t * 6.0F) * (1.0F - 0.2F * t * t);
            int tint = ARGB32.color(Math.round(opacity * 255.0F), color);
            ribbon(pose, buffer, previous, point, sideWidth, previousU, u, previousTint, tint);
            ribbon(pose, buffer, previous, point, upWidth, previousU, u, previousTint, tint);
            previous = point;
            previousU = u;
            previousTint = tint;
        }
    }

    private static void ribbon(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            Vec3 start,
            Vec3 end,
            Vec3 across,
            float startU,
            float endU,
            int startTint,
            int endTint) {
        edge(pose, buffer, start, across, 1.0F, startU, 0.0F, startTint);
        edge(pose, buffer, start, across, -1.0F, startU, 1.0F, startTint);
        edge(pose, buffer, end, across, -1.0F, endU, 1.0F, endTint);
        edge(pose, buffer, end, across, 1.0F, endU, 0.0F, endTint);
    }

    private static void edge(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            Vec3 center,
            Vec3 across,
            float sign,
            float u,
            float v,
            int color) {
        buffer.addVertex(pose, (float) (center.x + across.x * sign), (float) (center.y + across.y * sign), (float)
                        (center.z + across.z * sign))
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(EMISSIVE_LIGHT)
                .setNormal(pose, 0, 1, 0);
    }

    public static float time(long gameTime, float partialTicks) {
        return (Math.floorMod(gameTime, CLOCK_WRAP) + partialTicks) * TIME_UNITS_PER_TICK;
    }
}
