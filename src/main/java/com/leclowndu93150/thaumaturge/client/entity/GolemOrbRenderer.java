package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class GolemOrbRenderer extends EntityRenderer<EntityGolemOrb> {
    private static final RenderType ORB_TYPE = TCRenderTypes.fxAdditiveBlurred(ParticleTextures.PARTICLES);

    private static final int GRID = 32;
    private static final int WHITE_ROW = 7;
    private static final int RED_ROW = 6;
    private static final int FRAME_COUNT = 6;
    private static final float ALPHA = 0.8F;
    private static final float HALF = 0.5F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;

    public GolemOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            EntityGolemOrb entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        float bob = Mth.sin(entity.tickCount / 5.0F) * 0.2F + 0.2F;
        float scale = 1.0F + bob;
        int row = entity.isRed() ? RED_ROW : WHITE_ROW;
        int col = 1 + entity.tickCount % FRAME_COUNT;
        float texFrame = 1.0F / GRID;
        float u0 = col * texFrame;
        float v0 = row * texFrame;
        float u1 = u0 + texFrame;
        float v1 = v0 + texFrame;
        int tint = ARGB32.colorFromFloat(ALPHA, 1.0F, 1.0F, 1.0F);
        Vec3 origin = entity.getPosition(partialTicks);
        LateWorldRenderQueue.enqueue(origin, (latePose, lateBuffers) -> {
            latePose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            latePose.scale(scale, scale, scale);
            writeOrb(lateBuffers.getBuffer(ORB_TYPE), latePose.last().pose(), u0, v0, u1, v1, tint);
        });
    }

    private static void writeOrb(
            VertexConsumer buffer, Matrix4f mat, float u0, float v0, float u1, float v1, int tint) {
        buffer.addVertex(mat, -HALF, -HALF, 0.0F).setUv(u1, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, -HALF, HALF, 0.0F).setUv(u1, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, HALF, HALF, 0.0F).setUv(u0, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, HALF, -HALF, 0.0F).setUv(u0, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGolemOrb entity) {
        return ParticleTextures.PARTICLES;
    }
}
