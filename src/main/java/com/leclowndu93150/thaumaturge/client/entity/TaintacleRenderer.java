package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintacleModel;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TaintacleRenderer extends MobRenderer<AbstractTaintacle, TaintacleModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/entity/taintacle.png");
    private static final float HEIGHT_SCALE_DIVISOR = 3.0F;

    public TaintacleRenderer(EntityRendererProvider.Context context, float shadow) {
        super(context, new TaintacleModel(context.bakeLayer(TTModelLayers.TAINTACLE)), shadow);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractTaintacle entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AbstractTaintacle entity, PoseStack poseStack, float partialTick) {
        float s = entity.getBbHeight() / HEIGHT_SCALE_DIVISOR;
        poseStack.scale(s, s, s);
    }
}
