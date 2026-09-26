package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityBrainyHusk;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.BabyZombieModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public final class BrainyHuskRenderer extends AbstractZombieRenderer<EntityBrainyHusk, ZombieRenderState, ZombieModel<ZombieRenderState>> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/brainy_husk.png");

    public BrainyHuskRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.HUSK)), new BabyZombieModel<>(context.bakeLayer(ModelLayers.HUSK_BABY)),
                ArmorModelSet.bake(ModelLayers.HUSK_ARMOR, context.getModelSet(), ZombieModel::new), ArmorModelSet.bake(ModelLayers.HUSK_BABY_ARMOR, context.getModelSet(), BabyZombieModel::new));
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
