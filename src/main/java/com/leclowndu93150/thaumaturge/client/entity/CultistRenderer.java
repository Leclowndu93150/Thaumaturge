package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public final class CultistRenderer extends HumanoidMobRenderer<EntityCultist, HumanoidModel<EntityCultist>> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/cultist.png");
    private static final float SHADOW = 0.5F;

    public CultistRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(TCModelLayers.CULTIST)), SHADOW);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultist entity) {
        return TEXTURE;
    }
}
