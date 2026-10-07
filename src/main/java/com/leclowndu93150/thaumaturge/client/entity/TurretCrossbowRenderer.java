package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.CrossbowModel;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TurretCrossbowRenderer extends MobRenderer<EntityTurretCrossbow, CrossbowModel> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/crossbow.png");
    private static final float SHADOW = 0.5F;

    public TurretCrossbowRenderer(EntityRendererProvider.Context context) {
        super(context, new CrossbowModel(context.bakeLayer(TTModelLayers.TURRET_CROSSBOW)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTurretCrossbow entity) {
        return TEXTURE;
    }
}
