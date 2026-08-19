package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.entity.WispEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix4fc;

public final class WispRenderer extends EntityRenderer<WispEntity, WispRenderState> {
    private static final Identifier NODES = TCIds.rl("textures/misc/auranodes.png");

    private static final RenderType PARTICLES_TYPE = RenderType.create("tc_wisp_particles",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", ParticleTextures.PARTICLES).useLightmap().createRenderSetup());

    private static final RenderType NODES_TYPE = RenderType.create("tc_wisp_nodes",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", NODES).useLightmap().createRenderSetup());

    private static final int PARTICLE_GRID = 64;
    private static final int NODE_GRID = 32;
    private static final int CORE_FRAME_START = 512;
    private static final int HALO_FRAME_START = 320;
    private static final int NODE_FRAME_START = 800;
    private static final int FRAME_SPREAD = 16;
    private static final float CORE_SCALE = 0.4F;
    private static final float HALO_SCALE = 0.75F;
    private static final float NODE_SCALE = 0.75F;
    private static final float HALO_ALPHA = 0.25F;
    private static final float NODE_ALPHA = 0.5F;
    private static final float QUAD_HALF_FACTOR = 0.5F;
    private static final float CENTER_Y = 0.45F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public WispRenderState createRenderState() {
        return new WispRenderState();
    }

    @Override
    public void extractRenderState(WispEntity entity, WispRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tick = entity.tickCount;
        state.dead = entity.isDeadOrDying();
        Holder<IAspect> aspect = entity.aspect();
        state.color = aspect == null ? 0xFFFFFF : aspect.value().color();
    }

    @Override
    public void submit(WispRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.dead) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, CENTER_Y, 0.0F);
        poseStack.mulPose(camera.orientation);
        int frame = state.tick % FRAME_SPREAD;
        submitQuad(collector, poseStack, PARTICLES_TYPE, PARTICLE_GRID, CORE_FRAME_START + frame, CORE_SCALE, 0xFFFFFF, 1.0F);
        submitQuad(collector, poseStack, PARTICLES_TYPE, PARTICLE_GRID, HALO_FRAME_START + frame, HALO_SCALE, 0xFFFFFF, HALO_ALPHA);
        submitQuad(collector, poseStack, NODES_TYPE, NODE_GRID, NODE_FRAME_START + frame, NODE_SCALE, state.color, NODE_ALPHA);
        poseStack.popPose();
    }

    private static void submitQuad(SubmitNodeCollector collector, PoseStack poseStack, RenderType type, int grid, int frame, float scale, int color, float alpha) {
        float texFrame = 1.0F / grid;
        float u0 = (frame % grid) * texFrame;
        float v0 = (frame / grid) * texFrame;
        float u1 = u0 + texFrame;
        float v1 = v0 + texFrame;
        float half = scale * QUAD_HALF_FACTOR;
        int tint = ARGB.colorFromFloat(alpha, ARGB.red(color) / 255.0F, ARGB.green(color) / 255.0F, ARGB.blue(color) / 255.0F);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> addQuad(buffer, pose.pose(), half, u0, v0, u1, v1, tint));
    }

    private static void addQuad(VertexConsumer buffer, Matrix4fc mat, float half, float u0, float v0, float u1, float v1, int tint) {
        buffer.addVertex(mat, -half, -half, 0.0F).setUv(u1, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, -half, half, 0.0F).setUv(u1, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, half, half, 0.0F).setUv(u0, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, half, -half, 0.0F).setUv(u0, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
    }
}
