package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.FireBatModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityFireBat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class FireBatRenderer extends MobRenderer<EntityFireBat, FireBatModel<EntityFireBat>> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/firebat.png");
    private static final float SHADOW = 0.25F;
    private static final float SCALE = 1.0F;
    private static final int FULLBRIGHT_BLOCK_LIGHT = 15;

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBatModel<>(context.bakeLayer(TTModelLayers.FIRE_BAT)), SHADOW);
    }

    @Override
    protected int getBlockLightLevel(EntityFireBat entity, BlockPos pos) {
        return FULLBRIGHT_BLOCK_LIGHT;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFireBat entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EntityFireBat entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
