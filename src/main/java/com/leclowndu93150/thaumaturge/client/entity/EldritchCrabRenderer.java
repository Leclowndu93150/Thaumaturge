package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchCrabModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchCrab;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class EldritchCrabRenderer extends ScaledMobRenderer<EntityEldritchCrab, EldritchCrabRenderState, EldritchCrabModel> {
    private static final float CRAB_SIZE = 0.8F;
    private static final float CRAB_SHADOW = 0.5F;

    public EldritchCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchCrabModel(context.bakeLayer(TTModelLayers.ELDRITCH_CRAB)), TTIds.rl("textures/entity/crab.png"), CRAB_SIZE, CRAB_SHADOW);
    }

    @Override
    public EldritchCrabRenderState createRenderState() {
        return new EldritchCrabRenderState();
    }

    @Override
    public void extractRenderState(EntityEldritchCrab crab, EldritchCrabRenderState state, float partialTicks) {
        super.extractRenderState(crab, state, partialTicks);
        state.helm = crab.hasHelm();
        state.latched = crab.isPassenger() ? 1.0F : 0.0F;
        state.airborne = crab.onGround() || crab.isPassenger() ? 0.0F : 1.0F;
        state.pinch = crab.getAttackAnim(partialTicks);
        state.hurtTime = Math.max(0.0F, crab.hurtTime - partialTicks);
        state.shellBreakTime = Math.max(0.0F, crab.shellBreakTicks() - partialTicks);
    }
}
