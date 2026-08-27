package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.content.entity.WispEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class WispRenderer extends EntityRenderer<WispEntity> {
    private static final ResourceLocation NODES = TCIds.rl("textures/misc/auranodes.png");

    private static final RenderType NODES_TYPE = TCRenderTypes.fxAdditiveAlphaTest(NODES);

    private static final int NODE_GRID = 32;
    private static final int NODE_FRAME_START = 800;
    private static final int FRAME_SPREAD = 16;
    private static final float CORE_SCALE = 0.4F;
    private static final float AURA_SCALE = 0.7F;
    private static final float CORE_ALPHA = 0.9F;
    private static final float AURA_ALPHA = 0.4F;
    private static final float QUAD_HALF_FACTOR = 0.5F;
    private static final float CENTER_Y = 0.45F;
    private static final int LEGACY_LIGHT = LightTexture.pack(14, 0);

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            WispEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        if (entity.isDeadOrDying()) {
            return;
        }
        Holder<IAspect> aspect = entity.aspect();
        int color = aspect == null ? 0xFFFFFF : aspect.value().color();
        int frame = entity.tickCount % FRAME_SPREAD;
        Vec3 origin = entity.getPosition(partialTick).add(0.0, CENTER_Y, 0.0);
        LateWorldRenderQueue.enqueue(origin, (latePose, lateBuffers) -> {
            latePose.mulPose(
                    Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
            int nodeFrame = NODE_FRAME_START + frame;
            drawQuad(lateBuffers, latePose, NODES_TYPE, NODE_GRID, nodeFrame, AURA_SCALE, color, AURA_ALPHA);
            drawQuad(lateBuffers, latePose, NODES_TYPE, NODE_GRID, nodeFrame, CORE_SCALE, 0xFFFFFF, CORE_ALPHA);
        });
    }

    @Override
    public ResourceLocation getTextureLocation(WispEntity entity) {
        return NODES;
    }

    private static void drawQuad(
            MultiBufferSource buffers,
            PoseStack poseStack,
            RenderType type,
            int grid,
            int frame,
            float scale,
            int color,
            float alpha) {
        float texFrame = 1.0F / grid;
        float u0 = (frame % grid) * texFrame;
        float v0 = (frame / grid) * texFrame;
        float u1 = u0 + texFrame;
        float v1 = v0 + texFrame;
        float half = scale * QUAD_HALF_FACTOR;
        int tint = ARGB32.colorFromFloat(
                alpha, ARGB32.red(color) / 255.0F, ARGB32.green(color) / 255.0F, ARGB32.blue(color) / 255.0F);
        VertexConsumer buffer = buffers.getBuffer(type);
        Matrix4f mat = poseStack.last().pose();
        buffer.addVertex(mat, -half, -half, 0.0F).setUv(u1, v1).setColor(tint).setLight(LEGACY_LIGHT);
        buffer.addVertex(mat, -half, half, 0.0F).setUv(u1, v0).setColor(tint).setLight(LEGACY_LIGHT);
        buffer.addVertex(mat, half, half, 0.0F).setUv(u0, v0).setColor(tint).setLight(LEGACY_LIGHT);
        buffer.addVertex(mat, half, -half, 0.0F).setUv(u0, v1).setColor(tint).setLight(LEGACY_LIGHT);
    }
}
