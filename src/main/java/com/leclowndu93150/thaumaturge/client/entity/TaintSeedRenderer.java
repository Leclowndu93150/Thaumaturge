package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSeedModel;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class TaintSeedRenderer extends MobRenderer<AbstractTaintSeed, TaintSeedRenderState, TaintSeedModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TTIds.MODID, "textures/entity/taint_seed.png");
    private static final float HEIGHT_SCALE_DIVISOR = 2.0F;
    private static final float EMERGE_TICKS_PER_HEIGHT = 10.0F;

    public TaintSeedRenderer(EntityRendererProvider.Context context, float shadow) {
        super(context, new TaintSeedModel(context.bakeLayer(TTModelLayers.TAINT_SEED)), shadow);
    }

    @Override
    public Identifier getTextureLocation(TaintSeedRenderState state) {
        return TEXTURE;
    }

    @Override
    public TaintSeedRenderState createRenderState() {
        return new TaintSeedRenderState();
    }

    @Override
    public void extractRenderState(AbstractTaintSeed entity, TaintSeedRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.hurt = Math.max(0.0F, entity.hurtTime - partialTicks);
        state.attackAnim = entity.attackAnim;
        state.strikeTime = Math.max(0.0F, entity.strikeTicks() - partialTicks);
        state.emergence = Mth.clamp(state.ageInTicks / (state.boundingBoxHeight * EMERGE_TICKS_PER_HEIGHT), 0.0F, 1.0F);
    }

    @Override
    protected void scale(TaintSeedRenderState state, PoseStack poseStack) {
        float s = state.boundingBoxHeight / HEIGHT_SCALE_DIVISOR;
        poseStack.scale(s, s, s);
    }
}
