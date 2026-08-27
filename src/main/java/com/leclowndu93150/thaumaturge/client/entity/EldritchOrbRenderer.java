package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class EldritchOrbRenderer extends EntityRenderer<EntityEldritchOrb> {
    private static final RenderType RAY_TYPE = TCRenderTypes.SPARKLE_CULLED;
    private static final RenderType BILLBOARD_TYPE = TCRenderTypes.fxTranslucentBlurred(ParticleTextures.PARTICLES);

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
    public void render(
            EntityEldritchOrb entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        float ticks = entity.tickCount + partialTicks;
        float spin = ticks / 80.0F * 360.0F;
        float ramp = Math.min(ticks, 10.0F) / 10.0F;
        rayRandom.setSeed(RAY_SEED);
        poseStack.pushPose();
        VertexConsumer rayBuffer = buffers.getBuffer(RAY_TYPE);
        for (int i = 0; i < RAY_COUNT; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rayRandom.nextFloat() * 360.0F + spin));
            float fa = (rayRandom.nextFloat() * 20.0F + 5.0F) / 30.0F * ramp;
            float f4 = (rayRandom.nextFloat() * 2.0F + 1.0F) / 30.0F * ramp;
            addRay(rayBuffer, poseStack.last().pose(), fa, f4);
        }
        poseStack.popPose();
        float texFrame = 1.0F / GRID;
        float u0 = ((int) ticks % BILLBOARD_FRAMES) * texFrame;
        float v0 = BILLBOARD_ROW * texFrame;
        float u1 = u0 + texFrame;
        float v1 = v0 + texFrame;
        int tint = ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
        Vec3 origin = entity.getPosition(partialTicks);
        LateWorldRenderQueue.enqueue(origin, (latePose, lateBuffers) -> {
            latePose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            latePose.scale(BILLBOARD_SCALE, BILLBOARD_SCALE, BILLBOARD_SCALE);
            writeBillboard(
                    lateBuffers.getBuffer(BILLBOARD_TYPE), latePose.last().pose(), u0, v0, u1, v1, tint);
        });
    }

    private static void writeBillboard(
            VertexConsumer buffer, Matrix4f mat, float u0, float v0, float u1, float v1, int tint) {
        buffer.addVertex(mat, -HALF, -HALF, 0.0F).setUv(u1, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, -HALF, HALF, 0.0F).setUv(u1, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, HALF, HALF, 0.0F).setUv(u0, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, HALF, -HALF, 0.0F).setUv(u0, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEldritchOrb entity) {
        return ParticleTextures.PARTICLES;
    }

    private static void addRay(VertexConsumer buffer, Matrix4f mat, float fa, float f4) {
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
