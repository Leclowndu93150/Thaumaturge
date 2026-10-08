package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGolemModel;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class EldritchGolemRenderer extends ScaledMobRenderer<EntityEldritchGolem, EldritchGolemModel> {
    private static final float GOLEM_SIZE = 1.0F;
    private static final float GOLEM_SHADOW = 0.7F;

    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new EldritchGolemModel(context.bakeLayer(TTModelLayers.ELDRITCH_GOLEM)),
                TTIds.rl("textures/entity/eldritch_golem.png"),
                GOLEM_SIZE,
                GOLEM_SHADOW);
        addLayer(new CoreLayer(this, context.bakeLayer(TTModelLayers.ELDRITCH_GOLEM)));
    }

    private static final class CoreLayer extends RenderLayer<EntityEldritchGolem, EldritchGolemModel> {
        private final EldritchGolemModel core;

        CoreLayer(RenderLayerParent<EntityEldritchGolem, EldritchGolemModel> parent, ModelPart root) {
            super(parent);
            core = new EldritchGolemModel(root, EldritchGolemModel.Material.CORE);
        }

        @Override
        public void render(
                PoseStack pose,
                MultiBufferSource buffers,
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
                    buffers.getBuffer(
                            RenderType.entityTranslucentEmissive(TTIds.rl("textures/entity/eldritch_golem.png"))),
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    -1);
        }
    }
}
