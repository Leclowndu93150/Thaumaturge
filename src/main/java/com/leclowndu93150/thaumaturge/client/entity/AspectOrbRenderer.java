package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.wands.EntityAspectOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import org.joml.Matrix4fc;

public final class AspectOrbRenderer extends EntityRenderer<EntityAspectOrb, AspectOrbRenderer.State> {
    public static final class State extends EntityRenderState {
        public int tick;
        public int age;
        public int color;
    }

    private static final RenderType ORB_TYPE = RenderType.create("tc_aspect_orb",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", ParticleTextures.PARTICLES).useLightmap().createRenderSetup());

    private static final int FRAME_COUNT = 16;
    private static final int FRAMES_PER_TICK = 2;
    private static final float ROW_V0 = 0.5F;
    private static final float ROW_V1 = 0.5625F;
    private static final float BASE_SCALE = 0.1F;
    private static final float AGE_SCALE = 0.3F;
    private static final float ALPHA = 0.5F;
    private static final float HALF = 0.5F;
    private static final float Y_OFFSET = 0.25F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;

    public AspectOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityAspectOrb entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tick = entity.tickCount;
        state.age = entity.getAge();
        state.color = entity.getAspectColor();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);
        float scale = BASE_SCALE + AGE_SCALE * ((float) (EntityAspectOrb.MAX_AGE - state.age) / EntityAspectOrb.MAX_AGE);
        poseStack.scale(scale, scale, scale);
        int frame = state.tick * FRAMES_PER_TICK % FRAME_COUNT;
        float u0 = frame / (float) FRAME_COUNT;
        float u1 = (frame + 1) / (float) FRAME_COUNT;
        int tint = ARGB.color((int) (ALPHA * 255.0F), state.color);
        collector.submitCustomGeometry(poseStack, ORB_TYPE, (pose, buffer) -> {
            Matrix4fc mat = pose.pose();
            buffer.addVertex(mat, -HALF, -Y_OFFSET, 0.0F).setUv(u0, ROW_V1).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, HALF, -Y_OFFSET, 0.0F).setUv(u1, ROW_V1).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, HALF, 1.0F - Y_OFFSET, 0.0F).setUv(u1, ROW_V0).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, -HALF, 1.0F - Y_OFFSET, 0.0F).setUv(u0, ROW_V0).setColor(tint).setLight(EMISSIVE_LIGHT);
        });
        poseStack.popPose();
    }
}
