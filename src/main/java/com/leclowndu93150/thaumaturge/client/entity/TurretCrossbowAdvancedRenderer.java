package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbowAdvanced;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public final class TurretCrossbowAdvancedRenderer extends EntityRenderer<EntityTurretCrossbowAdvanced> {
    private static final ResourceLocation MODEL = TTIds.rl("models/mesh/crossbow_advanced.ttmesh");
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/crossbow_advanced.png");
    private static final float BASE_LIFT = 0.75F;
    private static final float SHADOW = 0.5F;
    private static final float HURT_JIGGLE_DIVISOR = 500.0F;
    private static final int HURT_TINT = ARGB32.colorFromFloat(1.0F, 1.0F, 0.5F, 0.5F);

    private final RandomSource jiggleRandom = RandomSource.create();

    public TurretCrossbowAdvancedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW;
    }

    @Override
    public void render(
            EntityTurretCrossbowAdvanced entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        TTMesh mesh = GolemMeshes.get(MODEL);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        boolean ridingMinecart = entity.getVehicle() instanceof AbstractMinecart;
        int hurtTime = entity.hurtTime;
        float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float loadProgress = entity.getLoadProgress(partialTicks);
        float swingAnim = entity.swingAnim;
        int color = -1;
        poseStack.pushPose();
        poseStack.translate(0.0F, BASE_LIFT, 0.0F);
        poseStack.pushPose();
        if (ridingMinecart) {
            poseStack.scale(0.66F, 0.75F, 0.66F);
        }
        renderPart(mesh, "legs", poseStack, buffer, packedLight, color);
        poseStack.popPose();
        poseStack.pushPose();
        if (hurtTime > 0) {
            color = HURT_TINT;
            float jiggle = hurtTime / HURT_JIGGLE_DIVISOR;
            poseStack.translate(
                    jiggleRandom.nextGaussian() * jiggle,
                    jiggleRandom.nextGaussian() * jiggle,
                    jiggleRandom.nextGaussian() * jiggle);
        }
        poseStack.mulPose(Axis.YN.rotationDegrees(headYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));
        renderPart(mesh, "mech", poseStack, buffer, packedLight, color);
        renderPart(mesh, "box", poseStack, buffer, packedLight, color);
        renderPart(mesh, "shield", poseStack, buffer, packedLight, color);
        renderPart(mesh, "brain", poseStack, buffer, packedLight, color);
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, Mth.sin(Mth.sqrt(loadProgress) * Mth.TWO_PI) / 12.0F);
        renderPart(mesh, "loader", poseStack, buffer, packedLight, color);
        poseStack.popPose();
        float bowSwing = Mth.sin(Mth.sqrt(swingAnim) * Mth.TWO_PI) * 20.0F;
        poseStack.translate(0.0, 0.0, 0.375);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(bowSwing));
        renderPart(mesh, "bow1", poseStack, buffer, packedLight, color);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(bowSwing));
        renderPart(mesh, "bow2", poseStack, buffer, packedLight, color);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTurretCrossbowAdvanced entity) {
        return TEXTURE;
    }

    private static void renderPart(
            TTMesh mesh, String name, PoseStack poseStack, VertexConsumer buffer, int light, int color) {
        for (TTMeshPart part : mesh.parts()) {
            if (name.equals(part.name())) {
                GolemMeshes.renderPart(part, poseStack.last(), buffer, light, color);
            }
        }
    }
}
