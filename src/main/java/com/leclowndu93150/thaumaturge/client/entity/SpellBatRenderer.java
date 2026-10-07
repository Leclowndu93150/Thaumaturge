package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpellBat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class SpellBatRenderer extends MobRenderer<EntitySpellBat, SpellBatRenderState, BatModel> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/spellbat.png");
    private static final float SHADOW = 0.25F;
    private static final float SCALE = 1F;
    private static final float Y_OFFSET = -0.1F;
    private static final int TINT_ALPHA = 128;
    private static final int FULLBRIGHT_BLOCK_LIGHT = 15;

    public SpellBatRenderer(EntityRendererProvider.Context context) {
        super(context, new BatModel(context.bakeLayer(TTModelLayers.FIRE_BAT)), SHADOW);
    }

    @Override
    public SpellBatRenderState createRenderState() {
        return new SpellBatRenderState();
    }

    @Override
    public void extractRenderState(EntitySpellBat entity, SpellBatRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isResting = false;
        state.flyAnimationState.copyFrom(entity.flyAnimationState);
        state.color = entity.getColor();
    }

    @Override
    protected int getModelTint(SpellBatRenderState state) {
        return ARGB.color(TINT_ALPHA, state.color);
    }

    @Override
    protected int getBlockLightLevel(EntitySpellBat entity, BlockPos pos) {
        return FULLBRIGHT_BLOCK_LIGHT;
    }

    @Override
    public Identifier getTextureLocation(SpellBatRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(SpellBatRenderState state, PoseStack poseStack) {
        poseStack.translate(0.0F, Y_OFFSET, 0.0F);
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
