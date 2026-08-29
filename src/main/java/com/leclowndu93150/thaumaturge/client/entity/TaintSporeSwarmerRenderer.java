package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeSwarmerModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSporeSwarmer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** TC4-style Swarmer shell renderer with a separately blended/full-bright pulsing core. */
public final class TaintSporeSwarmerRenderer extends MobRenderer<EntityTaintSporeSwarmer, TaintSporeSwarmerModel> {
    private static final float SHADOW = 0.25F;

    public TaintSporeSwarmerRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintSporeSwarmerModel(context.bakeLayer(TCModelLayers.TAINT_SPORE_SWARMER)), SHADOW);
        addLayer(new TaintSporeSwarmerCoreLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTaintSporeSwarmer entity) {
        return TaintSporeRenderer.TEXTURE;
    }
}
