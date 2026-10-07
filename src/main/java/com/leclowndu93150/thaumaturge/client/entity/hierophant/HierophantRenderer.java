package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public final class HierophantRenderer extends MobRenderer<EntityEldritchHierophant, HierophantRenderState, HierophantModel> {
    private static final float SHADOW_RADIUS = 1.0F;
    private static final float MOVEMENT_BLEND_SCALE = 8;
    private static final double CULLING_MARGIN = 3;

    public HierophantRenderer(EntityRendererProvider.Context context) {
        super(context, new HierophantModel(context.bakeLayer(TTModelLayers.ELDRITCH_HIEROPHANT)), SHADOW_RADIUS);
        addLayer(new HierophantCastingLayer(this));
        addLayer(new HierophantEyeLayer(this, context.bakeLayer(TTModelLayers.ELDRITCH_HIEROPHANT)));
    }

    @Override
    public HierophantRenderState createRenderState() {
        return new HierophantRenderState();
    }
    @Override
    public void extractRenderState(EntityEldritchHierophant entity, HierophantRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.action = entity.action();
        state.actionTicks = entity.actionAge(partialTick);
        state.awakened = entity.awakened();
        state.movement = Mth.clamp((float) entity.getDeltaMovement().horizontalDistance() * MOVEMENT_BLEND_SCALE, 0, 1);
        state.recoil = Math.max(0, entity.hurtTime - partialTick);
    }
    @Override
    protected void scale(HierophantRenderState state, PoseStack pose) {
        pose.scale(EntityEldritchHierophant.MODEL_SCALE, EntityEldritchHierophant.MODEL_SCALE, EntityEldritchHierophant.MODEL_SCALE);
    }
    @Override
    protected float getFlipDegrees() {
        return 0;
    }
    @Override
    protected AABB getBoundingBoxForCulling(EntityEldritchHierophant entity) {
        return super.getBoundingBoxForCulling(entity).inflate(CULLING_MARGIN);
    }
    @Override
    public Identifier getTextureLocation(HierophantRenderState state) {
        return HierophantTextures.BOSS;
    }
}
