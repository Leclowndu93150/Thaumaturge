package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSeedModel;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class TaintSeedRenderer extends MobRenderer<AbstractTaintSeed, TaintSeedRenderState, TaintSeedModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/entity/taint_seed.png");
    private static final float MODEL_Y_OFFSET = 1.501F;
    private static final float LIFT = 1.2F;
    private static final float HEIGHT_SCALE_DIVISOR = 2.0F;
    private static final float EMERGE_TICKS_PER_HEIGHT = 10.0F;
    private static final float HURT_DIVISOR = 200.0F;

    public TaintSeedRenderer(EntityRendererProvider.Context context, float shadow) {
        super(context, new TaintSeedModel(context.bakeLayer(TCModelLayers.TAINT_SEED)), shadow);
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
        state.hurt = entity.hurtTime / HURT_DIVISOR;
        state.attackAnim = entity.attackAnim;
    }

    @Override
    protected void scale(TaintSeedRenderState state, PoseStack poseStack) {
        float height = state.boundingBoxHeight;
        float emerge = 0.0F;
        float emergeTicks = height * EMERGE_TICKS_PER_HEIGHT;
        if (state.ageInTicks < emergeTicks) {
            emerge = (emergeTicks - state.ageInTicks) / emergeTicks * height;
        }
        float s = height / HEIGHT_SCALE_DIVISOR;
        poseStack.translate(0.0F, LIFT + emerge - MODEL_Y_OFFSET * (1.0F - s), 0.0F);
        poseStack.scale(s, s, s);
    }
}
