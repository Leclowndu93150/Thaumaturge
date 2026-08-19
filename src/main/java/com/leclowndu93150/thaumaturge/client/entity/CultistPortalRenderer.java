package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4fc;

public final class CultistPortalRenderer extends EntityRenderer<EntityCultistPortalLesser, CultistPortalRenderer.State> {
    public static final class State extends EntityRenderState {
        public boolean active;
        public float activeCounter;
        public int hurtTime;
        public int pulse;
        public float healthFraction;
        public float halfHeight;
    }

    private static final Identifier TEXTURE = TCIds.rl("textures/misc/cultist_portal.png");
    private static final RenderType PORTAL_TYPE = RenderType.create("tc_cultist_portal",
            RenderSetup.builder(TCRenderPipelines.FX_TRANSLUCENT).withTexture("Sampler0", TEXTURE).useLightmap().createRenderSetup());

    private static final int FRAMES = 16;
    private static final float FRAME_WIDTH = 0.0625F;
    private static final float BASE_SCALE_Y = 1.4F;
    private static final float SCALE_FACTOR = 1.25F;
    private static final float GROW_TICKS = 50.0F;
    private static final int LIGHT = 0x00F000DC;

    public CultistPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityCultistPortalLesser entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.active = entity.isActive();
        state.activeCounter = entity.activeCounter + partialTicks;
        state.hurtTime = entity.hurtTime;
        state.pulse = entity.pulse;
        state.healthFraction = entity.getHealth() / entity.getMaxHealth();
        state.halfHeight = entity.getBbHeight() / 2.0F;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        submitPortal(state, poseStack, collector, camera, BASE_SCALE_Y, SCALE_FACTOR);
    }

    static void submitPortal(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, float baseScaleY, float scaleFactor) {
        if (!state.active) {
            return;
        }
        float scaleY = baseScaleY;
        float grown = Math.min(GROW_TICKS, state.activeCounter);
        if (state.hurtTime > 0) {
            double d = Math.sin(state.hurtTime * 72.0 * Math.PI / 180.0);
            scaleY -= (float) (d / 4.0);
            grown += (float) (6.0 * d);
        }
        if (state.pulse > 0) {
            double d = Math.sin(state.pulse * 36.0 * Math.PI / 180.0);
            scaleY += (float) (d / 4.0);
            grown += (float) (12.0 * d);
        }
        float scale = grown / GROW_TICKS * scaleFactor;
        float m = (1.0F - state.healthFraction) / 3.0F;
        float bob = Mth.sin(state.activeCounter / (5.0F - 12.0F * m)) * m + m;
        float bob2 = Mth.sin(state.activeCounter / (6.0F - 15.0F * m)) * m + m;
        float alpha = 1.0F - bob;
        scaleY -= bob / 4.0F;
        scale -= bob2 / 3.0F;
        int frame = FRAMES - 1 - (int) state.activeCounter % FRAMES;
        float u0 = frame * FRAME_WIDTH;
        float u1 = u0 + FRAME_WIDTH;
        int tint = ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F);
        float sx = scale;
        float sy = scaleY;
        poseStack.pushPose();
        poseStack.translate(0.0F, state.halfHeight, 0.0F);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, PORTAL_TYPE, (pose, buffer) -> {
            Matrix4fc mat = pose.pose();
            buffer.addVertex(mat, -sx, -sy, 0.0F).setUv(u1, 0.0F).setColor(tint).setLight(LIGHT);
            buffer.addVertex(mat, -sx, sy, 0.0F).setUv(u1, 1.0F).setColor(tint).setLight(LIGHT);
            buffer.addVertex(mat, sx, sy, 0.0F).setUv(u0, 1.0F).setColor(tint).setLight(LIGHT);
            buffer.addVertex(mat, sx, -sy, 0.0F).setUv(u0, 0.0F).setColor(tint).setLight(LIGHT);
        });
        poseStack.popPose();
    }
}
