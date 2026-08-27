package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityBrainyDrowned;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class BrainyDrownedRenderer
        extends AbstractZombieRenderer<EntityBrainyDrowned, DrownedModel<EntityBrainyDrowned>> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/brainy_drowned.png");

    public BrainyDrownedRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new DrownedModel<>(context.bakeLayer(ModelLayers.DROWNED)),
                new DrownedModel<>(context.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)),
                new DrownedModel<>(context.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR)));
        this.addLayer(new BrainyDrownedOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBrainyDrowned entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(
            EntityBrainyDrowned entity,
            PoseStack poseStack,
            float bob,
            float yBodyRot,
            float partialTick,
            float scale) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
        float swimAmount = entity.getSwimAmount(partialTick);
        if (swimAmount > 0.0F) {
            float tilt = Mth.lerp(swimAmount, 0.0F, -10.0F - entity.getXRot());
            poseStack.rotateAround(Axis.XP.rotationDegrees(tilt), 0.0F, entity.getBbHeight() / 2.0F / scale, 0.0F);
        }
    }
}
