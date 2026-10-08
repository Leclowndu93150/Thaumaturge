package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class HierophantEyeLayer extends RenderLayer<EntityEldritchHierophant, HierophantModel> {
    private final HierophantModel eye;

    public HierophantEyeLayer(RenderLayerParent<EntityEldritchHierophant, HierophantModel> parent, ModelPart root) {
        super(parent);
        eye = new HierophantModel(root);
        eye.onlyEye();
    }

    @Override
    public void render(
            PoseStack pose,
            MultiBufferSource collector,
            int light,
            EntityEldritchHierophant entity,
            float limbSwing,
            float limbAmount,
            float partialTick,
            float age,
            float yaw,
            float pitch) {
        HierophantRenderState state = HierophantRenderState.of(entity, partialTick);
        if (state.isInvisible) {
            return;
        }
        eye.setupAnim(state);
        eye.renderToBuffer(
                pose,
                collector.getBuffer(HierophantRenderTypes.EYE),
                0xF000F0,
                OverlayTexture.NO_OVERLAY,
                state.awakened ? 0xFFFF6960 : -1);
    }
}
