package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public final class SpecialItemRenderer extends ItemEntityRenderer {
    private static final int CONE_COUNT_FANCY = 10;
    private static final int CONE_COUNT_FAST = 5;
    private static final float ROTATION_SPEED = 1.0F / 500.0F;
    private static final float SCALE_UP_TICKS = 10.0F;
    private static final float CONE_LIFT = 0.25F;

    private static final RenderType SPARKLE_TYPE = TTRenderTypes.SPARKLE_CULLED;

    private final RandomSource sparkleRandom = RandomSource.create();

    public SpecialItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            ItemEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        ItemStack item = entity.getItem();
        if (!item.isEmpty()) {
            float ageInTicks = entity.getAge() + partialTicks;
            float bob = Mth.sin(ageInTicks / 10.0F + entity.bobOffs) * 0.1F + 0.1F;
            float rotationProgress = ageInTicks * ROTATION_SPEED;
            float ageScale = Math.min(ageInTicks, SCALE_UP_TICKS) / SCALE_UP_TICKS;
            int coneCount = Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST
                    ? CONE_COUNT_FANCY
                    : CONE_COUNT_FAST;
            VertexConsumer buffer = buffers.getBuffer(SPARKLE_TYPE);
            poseStack.pushPose();
            poseStack.translate(0.0F, bob + CONE_LIFT, 0.0F);
            sparkleRandom.setSeed(187L);
            for (int i = 0; i < coneCount; i++) {
                float rx1 = sparkleRandom.nextFloat() * 360.0F;
                float ry1 = sparkleRandom.nextFloat() * 360.0F;
                float rz1 = sparkleRandom.nextFloat() * 360.0F;
                float rx2 = sparkleRandom.nextFloat() * 360.0F;
                float ry2 = sparkleRandom.nextFloat() * 360.0F;
                float rz2 = sparkleRandom.nextFloat() * 360.0F + rotationProgress * 360.0F;
                float fa = (sparkleRandom.nextFloat() * 20.0F + 5.0F) / 30.0F * ageScale;
                float f4 = (sparkleRandom.nextFloat() * 2.0F + 1.0F) / 30.0F * ageScale;
                poseStack.mulPose(Axis.XP.rotationDegrees(rx1));
                poseStack.mulPose(Axis.YP.rotationDegrees(ry1));
                poseStack.mulPose(Axis.ZP.rotationDegrees(rz1));
                poseStack.mulPose(Axis.XP.rotationDegrees(rx2));
                poseStack.mulPose(Axis.YP.rotationDegrees(ry2));
                poseStack.mulPose(Axis.ZP.rotationDegrees(rz2));
                Matrix4f mat = poseStack.last().pose();
                float bx1 = -0.866F * f4;
                float bz1 = -0.5F * f4;
                float bx2 = 0.866F * f4;
                float bz3 = f4;
                buffer.addVertex(mat, 0, 0, 0).setColor(1.0F, 1.0F, 1.0F, 1.0F);
                buffer.addVertex(mat, bx1, fa, bz1).setColor(1.0F, 0.0F, 1.0F, 0.0F);
                buffer.addVertex(mat, bx2, fa, bz1).setColor(1.0F, 0.0F, 1.0F, 0.0F);
                buffer.addVertex(mat, 0, 0, 0).setColor(1.0F, 1.0F, 1.0F, 1.0F);
                buffer.addVertex(mat, bx2, fa, bz1).setColor(1.0F, 0.0F, 1.0F, 0.0F);
                buffer.addVertex(mat, 0, fa, bz3).setColor(1.0F, 0.0F, 1.0F, 0.0F);
                buffer.addVertex(mat, 0, 0, 0).setColor(1.0F, 1.0F, 1.0F, 1.0F);
                buffer.addVertex(mat, 0, fa, bz3).setColor(1.0F, 0.0F, 1.0F, 0.0F);
                buffer.addVertex(mat, bx1, fa, bz1).setColor(1.0F, 0.0F, 1.0F, 0.0F);
            }
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
    }
}
