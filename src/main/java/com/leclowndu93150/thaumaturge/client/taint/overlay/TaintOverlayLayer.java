package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;

public final class TaintOverlayLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final int NO_TINT = -1;

    public TaintOverlayLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (entity.isInvisible() || !carriesTaintTrait(entity)) {
            return;
        }
        M model = this.getParentModel();
        TaintSkin skin = TaintSkins.get(model, this.getTextureLocation(entity));
        if (skin == null) {
            return;
        }
        RenderType skinType =
                skin.replacesBase() ? model.renderType(skin.texture()) : RenderType.entityTranslucent(skin.texture());
        model.renderToBuffer(
                poseStack,
                buffers.getBuffer(skinType),
                packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                NO_TINT);
        if (skin.glow() != null) {
            model.renderToBuffer(
                    poseStack,
                    buffers.getBuffer(RenderType.eyes(skin.glow())),
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    NO_TINT);
        }
    }

    private static boolean carriesTaintTrait(LivingEntity entity) {
        for (Holder<MobTrait> trait : MobTraits.traits(entity)) {
            if (trait.value().isTaint()) {
                return true;
            }
        }
        return false;
    }
}
