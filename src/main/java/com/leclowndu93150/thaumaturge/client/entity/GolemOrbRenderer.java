package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4fc;

public final class GolemOrbRenderer extends EntityRenderer<EntityGolemOrb, GolemOrbRenderer.State> {
    public static final class State extends EntityRenderState {
        public int tick;
        public boolean red;
    }

    private static final RenderType BLUE_ORB_TYPE = RenderType.create("tc_golem_orb",
            RenderSetup.builder(TTRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", ParticleTextures.GOLEM_ORB_BLUE).useLightmap().createRenderSetup());
    private static final RenderType RED_ORB_TYPE = RenderType.create("tc_golem_orb_red",
            RenderSetup.builder(TTRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", ParticleTextures.GOLEM_ORB_RED).useLightmap().createRenderSetup());

    private static final float ALPHA = 0.8F;
    private static final float HALF = 0.5F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;

    public GolemOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityGolemOrb entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tick = entity.tickCount;
        state.red = entity.isRed();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);
        float bob = Mth.sin(state.tick / 5.0F) * 0.2F + 0.2F;
        poseStack.scale(1.0F + bob, 1.0F + bob, 1.0F + bob);
        int frame = state.tick % ParticleTextures.GOLEM_ORB_FRAMES;
        float u0 = StripUv.u0(frame, ParticleTextures.GOLEM_ORB_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.GOLEM_ORB_FRAMES);
        float v0 = StripUv.V0;
        float v1 = StripUv.V1;
        int tint = ARGB.colorFromFloat(ALPHA, 1.0F, 1.0F, 1.0F);
        collector.submitCustomGeometry(poseStack, state.red ? RED_ORB_TYPE : BLUE_ORB_TYPE, (pose, buffer) -> {
            Matrix4fc mat = pose.pose();
            buffer.addVertex(mat, -HALF, -HALF, 0.0F).setUv(u1, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, -HALF, HALF, 0.0F).setUv(u1, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, HALF, HALF, 0.0F).setUv(u0, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, HALF, -HALF, 0.0F).setUv(u0, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
        });
        poseStack.popPose();
    }
}
