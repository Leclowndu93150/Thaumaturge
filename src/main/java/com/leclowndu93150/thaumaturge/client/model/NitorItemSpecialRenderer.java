package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.particle.ParticleSheet;
import com.leclowndu93150.thaumaturge.client.particle.TTParticleSheets;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.content.misc.nitor.BlockNitor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class NitorItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ParticleSheet CORE = TTParticleSheets.sheet("nitor_core");
    private static final ParticleSheet FLAME = TTParticleSheets.sheet("wisp_flame");
    private static final int FLAME_COUNT = 6;
    private static final float FLAME_LIFETIME = 12.0F;

    public NitorItemSpecialRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (IrisCompat.isSolidHandPass()
                || !(stack.getItem() instanceof BlockItem item)
                || !(item.getBlock() instanceof BlockNitor nitor)) {
            return;
        }
        int color = nitor.dyeColor();
        float ticks = (Util.getMillis() % 60000L) / 50.0F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.45F, 0.5F);
        float pulse = 0.2F + Mth.sin(ticks * 0.3F) * 0.01F;
        drawCross(poseStack, buffers, CORE, 0, pulse, color, 180);
        for (int i = 0; i < FLAME_COUNT; i++) {
            float age = (ticks + i * FLAME_LIFETIME / FLAME_COUNT) % FLAME_LIFETIME;
            float progress = age / FLAME_LIFETIME;
            float angle = i * Mth.TWO_PI / FLAME_COUNT;
            poseStack.pushPose();
            poseStack.translate(Mth.cos(angle + age * 0.1F) * 0.04F, age * 0.025F, Mth.sin(angle + age * 0.1F) * 0.04F);
            int frame = (int) age % FLAME.frames();
            drawCross(poseStack, buffers, FLAME, frame, 0.18F * (1.0F - progress), color, (int)
                    (160.0F * (1.0F - progress)));
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void drawCross(
            PoseStack poseStack,
            MultiBufferSource buffers,
            ParticleSheet sheet,
            int frame,
            float radius,
            int color,
            int alpha) {
        float u0 = sheet.u0(frame);
        float u1 = sheet.u1(frame);
        VertexConsumer buffer = buffers.getBuffer(TTFlatRenderTypes.entityAdditiveFlat(sheet.texture()));
        int tint = (alpha << 24) | (color & 0xFFFFFF);
        poseStack.pushPose();
        drawQuad(poseStack.last(), buffer, radius, tint, u0, u1);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        drawQuad(poseStack.last(), buffer, radius, tint, u0, u1);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        drawQuad(poseStack.last(), buffer, radius, tint, u0, u1);
        poseStack.popPose();
    }

    private static void drawQuad(
            PoseStack.Pose pose, VertexConsumer buffer, float radius, int tint, float u0, float u1) {
        vertex(pose, buffer, -radius, -radius, u0, 1.0F, tint);
        vertex(pose, buffer, -radius, radius, u0, 0.0F, tint);
        vertex(pose, buffer, radius, radius, u1, 0.0F, tint);
        vertex(pose, buffer, radius, -radius, u1, 1.0F, tint);
    }

    private static void vertex(
            PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float u, float v, int tint) {
        buffer.addVertex(pose.pose(), x, y, 0.0F)
                .setColor(tint)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
