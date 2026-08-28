package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.wands.EntityAspectOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class AspectOrbRenderer extends EntityRenderer<EntityAspectOrb> {
    private static final RenderType ORB_TYPE = TCRenderTypes.fxAdditiveBlurred(ParticleTextures.PARTICLES);

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
    public void render(
            EntityAspectOrb entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        float scale = BASE_SCALE
                + AGE_SCALE * ((float) (EntityAspectOrb.MAX_AGE - entity.getAge()) / EntityAspectOrb.MAX_AGE);
        int frame = entity.tickCount * FRAMES_PER_TICK % FRAME_COUNT;
        float u0 = frame / (float) FRAME_COUNT;
        float u1 = (frame + 1) / (float) FRAME_COUNT;
        int tint = ARGB32.color((int) (ALPHA * 255.0F), entity.getAspectColor());
        Vec3 origin = entity.getPosition(partialTicks);
        LateWorldRenderQueue.enqueue(origin, (latePose, lateBuffers) -> {
            latePose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            latePose.scale(scale, scale, scale);
            writeOrb(lateBuffers.getBuffer(ORB_TYPE), latePose.last().pose(), u0, u1, tint);
        });
    }

    private static void writeOrb(VertexConsumer buffer, Matrix4f mat, float u0, float u1, int tint) {
        buffer.addVertex(mat, -HALF, -Y_OFFSET, 0.0F)
                .setUv(u0, ROW_V1)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, HALF, -Y_OFFSET, 0.0F)
                .setUv(u1, ROW_V1)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, HALF, 1.0F - Y_OFFSET, 0.0F)
                .setUv(u1, ROW_V0)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, -HALF, 1.0F - Y_OFFSET, 0.0F)
                .setUv(u0, ROW_V0)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAspectOrb entity) {
        return ParticleTextures.PARTICLES;
    }
}
