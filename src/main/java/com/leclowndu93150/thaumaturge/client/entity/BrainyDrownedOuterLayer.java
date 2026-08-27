package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Drowned;

public final class BrainyDrownedOuterLayer<T extends Drowned> extends RenderLayer<T, DrownedModel<T>> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/brainy_drowned_outer_layer.png");

    private final DrownedModel<T> model;

    public BrainyDrownedOuterLayer(RenderLayerParent<T, DrownedModel<T>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new DrownedModel<>(modelSet.bakeLayer(ModelLayers.DROWNED_OUTER_LAYER));
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        coloredCutoutModelCopyLayerRender(
                this.getParentModel(),
                this.model,
                TEXTURE,
                poseStack,
                buffer,
                packedLight,
                entity,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch,
                partialTick,
                -1);
    }
}
