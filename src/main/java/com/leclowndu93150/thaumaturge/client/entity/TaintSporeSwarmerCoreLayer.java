package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeSwarmerModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSporeSwarmer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Full-bright inner cube used by the Swarmer model. */
final class TaintSporeSwarmerCoreLayer
        extends RenderLayer<EntityTaintSporeSwarmer, TaintSporeSwarmerModel<EntityTaintSporeSwarmer>> {
    private final TaintSporeSwarmerModel<EntityTaintSporeSwarmer> core;

    TaintSporeSwarmerCoreLayer(
            RenderLayerParent<EntityTaintSporeSwarmer, TaintSporeSwarmerModel<EntityTaintSporeSwarmer>> renderer,
            EntityModelSet modelSet) {
        super(renderer);
        this.core = new TaintSporeSwarmerModel<>(modelSet.bakeLayer(TTModelLayers.TAINT_SPORE_SWARMER_CORE));
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
        core.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(TaintSporeRenderer.TEXTURE));
        core.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
    }
}
