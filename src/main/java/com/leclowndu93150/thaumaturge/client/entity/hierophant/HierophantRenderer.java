package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class HierophantRenderer extends MobRenderer<EntityEldritchHierophant, HierophantModel> {
    private static final float SHADOW_RADIUS = 1.0F;
    private static final float MOVEMENT_BLEND_SCALE = 8;
    private static final double CULLING_MARGIN = 3;

    public HierophantRenderer(EntityRendererProvider.Context context) {
        super(context, new HierophantModel(context.bakeLayer(TTModelLayers.ELDRITCH_HIEROPHANT)), SHADOW_RADIUS);
        addLayer(new HierophantCastingLayer(this));
        addLayer(new HierophantEyeLayer(this, context.bakeLayer(TTModelLayers.ELDRITCH_HIEROPHANT)));
    }

    @Override
    protected void scale(EntityEldritchHierophant entity, PoseStack pose, float partialTick) {
        pose.scale(
                EntityEldritchHierophant.MODEL_SCALE,
                EntityEldritchHierophant.MODEL_SCALE,
                EntityEldritchHierophant.MODEL_SCALE);
    }

    @Override
    protected float getFlipDegrees(EntityEldritchHierophant entity) {
        return 0;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEldritchHierophant entity) {
        return HierophantTextures.BOSS;
    }

    @Override
    public boolean shouldRender(EntityEldritchHierophant entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z)
                && frustum.isVisible(entity.getBoundingBox().inflate(CULLING_MARGIN));
    }
}
