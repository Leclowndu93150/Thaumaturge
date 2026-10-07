package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGolemModel;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class EldritchGolemRenderer extends MobRenderer<EntityEldritchGolem, EldritchGolemModel> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/eldritch_golem.png");
    private static final float SHADOW = 0.7F;
    private static final float SCALE = 1.0F;

    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGolemModel(context.bakeLayer(TTModelLayers.ELDRITCH_GOLEM)), SHADOW);
        addLayer(new CoreLayer(this, context.bakeLayer(TTModelLayers.ELDRITCH_GOLEM)));
    }

    @Override
    protected void scale(EntityEldritchGolem entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEldritchGolem entity) {
        return TEXTURE;
    }

    private static final class CoreLayer
            extends net.minecraft.client.renderer.entity.layers.RenderLayer<EntityEldritchGolem, EldritchGolemModel> {
        private final EldritchGolemModel core;

        CoreLayer(
                net.minecraft.client.renderer.entity.RenderLayerParent<EntityEldritchGolem, EldritchGolemModel> parent,
                net.minecraft.client.model.geom.ModelPart root) {
            super(parent);
            core = new EldritchGolemModel(root, EldritchGolemModel.Material.CORE);
        }

        @Override
        public void render(
                PoseStack pose,
                net.minecraft.client.renderer.MultiBufferSource buffers,
                int light,
                EntityEldritchGolem entity,
                float limbSwing,
                float limbSwingAmount,
                float partialTicks,
                float ageInTicks,
                float netHeadYaw,
                float headPitch) {
            if (entity.isInvisible()) return;
            core.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            core.renderToBuffer(
                    pose,
                    buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucentEmissive(TEXTURE)),
                    net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    -1);
        }
    }
}
