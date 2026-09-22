package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSpider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TaintSpiderRenderer extends SpiderRenderer<EntityTaintSpider> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/taint_spider.png");

    public TaintSpiderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTaintSpider entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EntityTaintSpider entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.4F, 0.4F, 0.4F);
    }
}
