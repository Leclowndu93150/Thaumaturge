package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4fc;

public final class EldritchOrbRenderer extends EntityRenderer<EntityEldritchOrb, EldritchOrbRenderer.State> {
    public static final class State extends EntityRenderState {
        public float ticks;
    }

    private static final RenderType RAY_TYPE = RenderType.create("tc_eldritch_orb_ray", RenderSetup.builder(TCRenderPipelines.SPARKLE_CULLED).createRenderSetup());
    private static final RenderType BILLBOARD_TYPE = RenderType.create("tc_eldritch_orb",
            RenderSetup.builder(TCRenderPipelines.FX_TRANSLUCENT).withTexture("Sampler0", ParticleTextures.PARTICLES).useLightmap().createRenderSetup());

    private static final long RAY_SEED = 187L;
    private static final int RAY_COUNT = 12;
    private static final int GRID = 64;
    private static final int BILLBOARD_ROW = 3;
    private static final int BILLBOARD_FRAMES = 13;
    private static final float BILLBOARD_SCALE = 0.75F;
    private static final float HALF = 0.5F;
    private static final float EDGE_GRAY = 0.75F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;

    private final RandomSource rayRandom = RandomSource.create();

    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityEldritchOrb entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.ticks = entity.tickCount + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        float spin = state.ticks / 80.0F * 360.0F;
        float ramp = Math.min(state.ticks, 10.0F) / 10.0F;
        rayRandom.setSeed(RAY_SEED);
        poseStack.pushPose();
        for (int i = 0; i < RAY_COUNT; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rayRandom.nextFloat() * 360.0F + spin));
            float fa = (rayRandom.nextFloat() * 20.0F + 5.0F) / 30.0F * ramp;
            float f4 = (rayRandom.nextFloat() * 2.0F + 1.0F) / 30.0F * ramp;
            collector.submitCustomGeometry(poseStack, RAY_TYPE, (pose, buffer) -> addRay(buffer, pose.pose(), fa, f4));
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);
        poseStack.scale(BILLBOARD_SCALE, BILLBOARD_SCALE, BILLBOARD_SCALE);
        float texFrame = 1.0F / GRID;
        float u0 = ((int) state.ticks % BILLBOARD_FRAMES) * texFrame;
        float v0 = BILLBOARD_ROW * texFrame;
        float u1 = u0 + texFrame;
        float v1 = v0 + texFrame;
        int tint = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
        collector.submitCustomGeometry(poseStack, BILLBOARD_TYPE, (pose, buffer) -> {
            Matrix4fc mat = pose.pose();
            buffer.addVertex(mat, -HALF, -HALF, 0.0F).setUv(u1, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, -HALF, HALF, 0.0F).setUv(u1, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, HALF, HALF, 0.0F).setUv(u0, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, HALF, -HALF, 0.0F).setUv(u0, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
        });
        poseStack.popPose();
    }

    private static void addRay(VertexConsumer buffer, Matrix4fc mat, float fa, float f4) {
        float bx1 = -0.866F * f4;
        float bz1 = -0.5F * f4;
        float bx2 = 0.866F * f4;
        buffer.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.addVertex(mat, bx1, fa, bz1).setColor(EDGE_GRAY, EDGE_GRAY, EDGE_GRAY, 0.0F);
        buffer.addVertex(mat, bx2, fa, bz1).setColor(EDGE_GRAY, EDGE_GRAY, EDGE_GRAY, 0.0F);
        buffer.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.addVertex(mat, bx2, fa, bz1).setColor(EDGE_GRAY, EDGE_GRAY, EDGE_GRAY, 0.0F);
        buffer.addVertex(mat, 0.0F, fa, f4).setColor(EDGE_GRAY, EDGE_GRAY, EDGE_GRAY, 0.0F);
        buffer.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.addVertex(mat, 0.0F, fa, f4).setColor(EDGE_GRAY, EDGE_GRAY, EDGE_GRAY, 0.0F);
        buffer.addVertex(mat, bx1, fa, bz1).setColor(EDGE_GRAY, EDGE_GRAY, EDGE_GRAY, 0.0F);
    }
}
