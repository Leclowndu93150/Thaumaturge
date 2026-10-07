package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintacleModel;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class TaintacleRenderer extends MobRenderer<AbstractTaintacle, TaintacleRenderState, TaintacleModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TTIds.MODID, "textures/entity/taintacle.png");
    private static final float HEIGHT_SCALE_DIVISOR = 3.0F;
    private static final float EMERGE_TICKS_PER_HEIGHT = 10.0F;

    public TaintacleRenderer(EntityRendererProvider.Context context, float shadow) {
        super(context, new TaintacleModel(context.bakeLayer(TTModelLayers.TAINTACLE)), shadow);
    }

    @Override
    public Identifier getTextureLocation(TaintacleRenderState state) {
        return TEXTURE;
    }

    @Override
    public TaintacleRenderState createRenderState() {
        return new TaintacleRenderState();
    }

    @Override
    public void extractRenderState(AbstractTaintacle entity, TaintacleRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flail = entity.flailIntensity;
        state.hurt = Math.max(0.0F, entity.hurtTime - partialTicks);
        state.enrage = entity.enrage();
        state.strikeTime = Math.max(0.0F, entity.strikeTicks() - partialTicks);
        state.emergence = Mth.clamp(state.ageInTicks / (state.boundingBoxHeight * EMERGE_TICKS_PER_HEIGHT), 0.0F, 1.0F);
    }

    @Override
    protected void scale(TaintacleRenderState state, PoseStack poseStack) {
        float s = state.boundingBoxHeight / HEIGHT_SCALE_DIVISOR;
        poseStack.scale(s, s, s);
    }
}
