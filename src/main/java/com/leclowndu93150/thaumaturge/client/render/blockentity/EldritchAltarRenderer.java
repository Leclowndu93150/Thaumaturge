package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.particle.ParticleSheet;
import com.leclowndu93150.thaumaturge.client.particle.TTParticleSheets;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.content.eldritch.altar.BlockEntityEldritchAltar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class EldritchAltarRenderer implements BlockEntityRenderer<BlockEntityEldritchAltar> {
    private static final int WISP_COUNT = 14;
    private static final float WISP_LIFETIME = 22.0F;
    private static final ParticleSheet WISPS = TTParticleSheets.sheet("wispy_mote");
    private static final RenderType RENDER_TYPE = TTRenderTypes.srcAlphaAdditiveTextured(WISPS.texture());

    private final EldritchCapRenderer<BlockEntityEldritchAltar> body;

    public EldritchAltarRenderer(BlockEntityRendererProvider.Context context) {
        body = new EldritchCapRenderer<>(
                context,
                EldritchCapRenderer.ALTAR_MODEL,
                EldritchCapRenderer.ALTAR_TEXTURE,
                EldritchCapRenderer.ALTAR_TEXTURE,
                BlockEntityEldritchAltar::getEyes);
    }

    @Override
    public void render(
            BlockEntityEldritchAltar altar,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        body.render(altar, partialTick, poseStack, buffers, light, overlay);
        long start = altar.ritualStart();
        var level = altar.getLevel();
        if (start < 0 || level == null) return;

        float time = Math.max(0L, level.getGameTime() - start) + partialTick;
        VertexConsumer buffer = buffers.getBuffer(RENDER_TYPE);
        RandomSource random = RandomSource.create(start);
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 anchor = Vec3.atCenterOf(altar.getBlockPos());
        for (int i = 0; i < WISP_COUNT; i++) {
            float age = (time + i * WISP_LIFETIME / WISP_COUNT) % WISP_LIFETIME;
            float progress = age / WISP_LIFETIME;
            float cycle = (float) Math.floor((time + i * WISP_LIFETIME / WISP_COUNT) / WISP_LIFETIME);
            float angle = random.nextFloat() * Mth.TWO_PI + cycle * 2.4F;
            float radius = 0.32F + random.nextFloat() * 0.52F;
            float height = 0.92F + random.nextFloat() * 0.58F + progress * 0.36F;
            float size = Mth.lerp(progress, 0.23F, 0.11F);
            int alpha = (int) (Mth.sin(progress * Mth.PI) * 205);
            int frame = (int) age % WISPS.frames();
            float u0 = WISPS.u0(frame);
            float u1 = WISPS.u1(frame);

            poseStack.pushPose();
            poseStack.translate(0.5F + Mth.cos(angle) * radius, height, 0.5F + Mth.sin(angle) * radius);
            ClientSpaces.faceCamera(level, anchor, poseStack, camera.rotation());
            PoseStack.Pose pose = poseStack.last();
            vertex(buffer, pose, -size, -size, u1, 1.0F, alpha);
            vertex(buffer, pose, -size, size, u1, 0.0F, alpha);
            vertex(buffer, pose, size, size, u0, 0.0F, alpha);
            vertex(buffer, pose, size, -size, u0, 1.0F, alpha);
            poseStack.popPose();
        }
    }

    private static void vertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int alpha) {
        buffer.addVertex(pose.pose(), x, y, 0.0F).setUv(u, v).setColor(176, 71, 238, alpha);
    }
}
