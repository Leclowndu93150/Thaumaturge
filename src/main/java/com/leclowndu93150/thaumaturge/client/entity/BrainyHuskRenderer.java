package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityBrainyHusk;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class BrainyHuskRenderer extends AbstractZombieRenderer<EntityBrainyHusk, ZombieModel<EntityBrainyHusk>> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/brainy_husk.png");

    public BrainyHuskRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.HUSK)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.HUSK_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.HUSK_OUTER_ARMOR)));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBrainyHusk entity) {
        return TEXTURE;
    }
}
