package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityFireBat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public final class FireBatRenderer extends MobRenderer<EntityFireBat, BatRenderState, BatModel> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/firebat.png");
    private static final float SHADOW = 0.25F;
    private static final float SCALE = 1F;
    private static final int FULLBRIGHT_BLOCK_LIGHT = 15;

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new BatModel(context.bakeLayer(TTModelLayers.FIRE_BAT)), SHADOW);
    }

    @Override
    public BatRenderState createRenderState() {
        return new BatRenderState();
    }

    @Override
    public void extractRenderState(EntityFireBat entity, BatRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isResting = entity.isHanging();
        state.flyAnimationState.copyFrom(entity.flyAnimationState);
        state.restAnimationState.copyFrom(entity.restAnimationState);
    }

    @Override
    protected int getBlockLightLevel(EntityFireBat entity, BlockPos pos) {
        return FULLBRIGHT_BLOCK_LIGHT;
    }

    @Override
    public Identifier getTextureLocation(BatRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(BatRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
