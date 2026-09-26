package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.BabyDrownedModel;
import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public final class BrainyDrownedOuterLayer extends RenderLayer<ZombieRenderState, DrownedModel> {
    private static final Identifier OUTER_LAYER = TCIds.rl("textures/entity/brainy_drowned_outer_layer.png");

    private final DrownedModel model;
    private final DrownedModel babyModel;

    public BrainyDrownedOuterLayer(RenderLayerParent<ZombieRenderState, DrownedModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new DrownedModel(modelSet.bakeLayer(ModelLayers.DROWNED_OUTER_LAYER));
        this.babyModel = new BabyDrownedModel(modelSet.bakeLayer(ModelLayers.DROWNED_BABY_OUTER_LAYER));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, ZombieRenderState state, float yRot, float xRot) {
        coloredCutoutModelCopyLayerRender(state.isBaby ? this.babyModel : this.model, OUTER_LAYER, poseStack, collector, lightCoords, state, -1, 1);
    }
}
