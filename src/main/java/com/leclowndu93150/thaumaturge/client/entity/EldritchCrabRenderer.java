package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchCrabModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchCrab;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class EldritchCrabRenderer extends MobRenderer<EntityEldritchCrab, EldritchCrabModel> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/crab.png");
    private static final float SHADOW = 0.5F;
    private static final float SCALE = 0.8F;

    public EldritchCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchCrabModel(context.bakeLayer(TTModelLayers.ELDRITCH_CRAB)), SHADOW);
    }

    @Override
    protected void scale(EntityEldritchCrab entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEldritchCrab entity) {
        return TEXTURE;
    }
}
