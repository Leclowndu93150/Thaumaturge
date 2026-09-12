package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeSwarmerModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSporeSwarmer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/** Full-bright inner cube used by the legacy Swarmer model. */
final class TaintSporeSwarmerCoreLayer extends RenderLayer<EntityTaintSporeSwarmer, TaintSporeSwarmerModel> {
    private static final float SIZE_SCALE = 0.07F;
    private static final float PULSE_SCALE = 0.025F;
    private static final float LEGACY_LIFT = 1.6F;
    private final ModelPart core;

    TaintSporeSwarmerCoreLayer(
            RenderLayerParent<EntityTaintSporeSwarmer, TaintSporeSwarmerModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.core = modelSet.bakeLayer(TCModelLayers.TAINT_SPORE_SWARMER_CORE).getChild("core");
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            EntityTaintSporeSwarmer entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        core.resetPose();
        float intensity = entity.hurtTime > 0 ? 0.04F : 0.02F;
        core.xRot = intensity * Mth.sin(ageInTicks * 0.05F);
        core.zRot = intensity * Mth.sin(ageInTicks * 0.10F);

        float base = SIZE_SCALE * entity.getDisplaySize(partialTick);
        float pulse = PULSE_SCALE * Mth.sin(ageInTicks * 0.075F);
        float horizontal = Math.max(0.01F, base + pulse);
        float vertical = Math.max(0.01F, base - pulse);

        poseStack.pushPose();
        // Preserve the TC4 core transform: a lifted, pulsing cube inside the stationary outer shell.
        poseStack.translate(0.0F, LEGACY_LIFT, 0.0F);
        poseStack.scale(horizontal, vertical, horizontal);
        poseStack.translate(0.0F, -vertical / 2.0F, 0.0F);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(TaintSporeRenderer.TEXTURE));
        core.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }
}
