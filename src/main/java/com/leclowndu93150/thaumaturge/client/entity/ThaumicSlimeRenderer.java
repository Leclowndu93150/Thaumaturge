package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ThaumicSlimeRenderer extends MobRenderer<ThaumicSlime, SlimeModel<ThaumicSlime>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/entity/thaumic_slime.png");

    public ThaumicSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
        this.addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    protected float getShadowRadius(ThaumicSlime entity) {
        return entity.getSize() * 0.25F;
    }

    @Override
    protected void scale(ThaumicSlime entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = entity.getSize();
        float squish = Mth.lerp(partialTick, entity.oSquish, entity.squish) / (size * 0.5F + 1.0F);
        float stretch = 1.0F / (squish + 1.0F);
        poseStack.scale(stretch * size, 1.0F / stretch * size, stretch * size);
    }

    @Override
    public ResourceLocation getTextureLocation(ThaumicSlime entity) {
        return TEXTURE;
    }
}
