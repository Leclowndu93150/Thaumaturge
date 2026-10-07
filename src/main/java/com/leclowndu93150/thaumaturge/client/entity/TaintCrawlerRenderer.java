package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TaintCrawlerRenderer extends MobRenderer<EntityTaintCrawler, SilverfishModel<EntityTaintCrawler>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/entity/taint_crawler.png");
    private static final float CRAWLER_SCALE = 0.7F;

    public TaintCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel<>(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTaintCrawler entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EntityTaintCrawler entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(CRAWLER_SCALE, CRAWLER_SCALE, CRAWLER_SCALE);
    }
}
