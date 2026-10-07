package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSeedModel;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TaintSeedRenderer extends MobRenderer<AbstractTaintSeed, TaintSeedModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/entity/taint_seed.png");
    private static final float HEIGHT_SCALE_DIVISOR = 2.0F;

    public TaintSeedRenderer(EntityRendererProvider.Context context, float shadow) {
        super(context, new TaintSeedModel(context.bakeLayer(TTModelLayers.TAINT_SEED)), shadow);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractTaintSeed entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AbstractTaintSeed entity, PoseStack poseStack, float partialTick) {
        float s = entity.getBbHeight() / HEIGHT_SCALE_DIVISOR;
        poseStack.scale(s, s, s);
    }
}
