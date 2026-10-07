package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.particle.ParticleSheet;
import com.leclowndu93150.thaumaturge.client.particle.TTParticleSheets;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class NitorItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private static final ParticleSheet CORE = TTParticleSheets.sheet("nitor_core");
    private static final ParticleSheet FLAME = TTParticleSheets.sheet("wisp_flame");
    private static final int FLAME_COUNT = 6;
    private static final float FLAME_LIFETIME = 12.0F;

    private final int color;

    public NitorItemSpecialRenderer(int color) {
        this.color = color;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (IrisCompat.isSolidHandPass()) {
            return;
        }
        float ticks = (Util.getMillis() % 60000L) / 50.0F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.45F, 0.5F);
        float pulse = 0.2F + Mth.sin(ticks * 0.3F) * 0.01F;
        submitCross(poseStack, collector, CORE, 0, pulse, ARGB.color(180, color));
        for (int i = 0; i < FLAME_COUNT; i++) {
            float age = (ticks + i * FLAME_LIFETIME / FLAME_COUNT) % FLAME_LIFETIME;
            float progress = age / FLAME_LIFETIME;
            float angle = i * Mth.TWO_PI / FLAME_COUNT;
            poseStack.pushPose();
            poseStack.translate(Mth.cos(angle + age * 0.1F) * 0.04F, age * 0.025F, Mth.sin(angle + age * 0.1F) * 0.04F);
            int frame = (int) age % FLAME.frames();
            int tint = ARGB.color((int) (160.0F * (1.0F - progress)), color);
            submitCross(poseStack, collector, FLAME, frame, 0.18F * (1.0F - progress), tint);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void submitCross(PoseStack poseStack, SubmitNodeCollector collector, ParticleSheet sheet, int frame, float radius, int tint) {
        float u0 = sheet.u0(frame);
        float u1 = sheet.u1(frame);
        poseStack.pushPose();
        submitQuad(poseStack, collector, sheet, radius, tint, u0, u1);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        submitQuad(poseStack, collector, sheet, radius, tint, u0, u1);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        submitQuad(poseStack, collector, sheet, radius, tint, u0, u1);
        poseStack.popPose();
    }

    private static void submitQuad(PoseStack poseStack, SubmitNodeCollector collector, ParticleSheet sheet, float radius, int tint, float u0, float u1) {
        collector.submitCustomGeometry(poseStack, TTFlatRenderTypes.entityAdditiveFlat(sheet.texture()), (pose, buffer) -> {
            vertex(pose, buffer, -radius, -radius, u0, 1.0F, tint);
            vertex(pose, buffer, -radius, radius, u0, 0.0F, tint);
            vertex(pose, buffer, radius, radius, u1, 0.0F, tint);
            vertex(pose, buffer, radius, -radius, u1, 1.0F, tint);
        });
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float u, float v, int tint) {
        buffer.addVertex(pose.pose(), x, y, 0.0F).setColor(tint).setUv(u, v).setOverlay(0).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.28F, 0.25F, 0.28F));
        consumer.accept(new Vector3f(0.72F, 0.8F, 0.72F));
    }

    public record Unbaked(int color) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = Codec.INT.fieldOf("color").xmap(Unbaked::new, Unbaked::color);

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new NitorItemSpecialRenderer(color);
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
