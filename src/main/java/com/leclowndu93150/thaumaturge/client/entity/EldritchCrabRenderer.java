package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchCrabModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchCrab;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class EldritchCrabRenderer extends ScaledMobRenderer<EntityEldritchCrab, EldritchCrabRenderState, EldritchCrabModel> {
    private static final float CRAB_SIZE = 0.8F;
    private static final float CRAB_SHADOW = 0.5F;

    public EldritchCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchCrabModel(context.bakeLayer(TCModelLayers.ELDRITCH_CRAB)), TCIds.rl("textures/entity/crab.png"), CRAB_SIZE, CRAB_SHADOW);
    }

    @Override
    public EldritchCrabRenderState createRenderState() {
        return new EldritchCrabRenderState();
    }

    @Override
    public void extractRenderState(EntityEldritchCrab crab, EldritchCrabRenderState state, float partialTicks) {
        super.extractRenderState(crab, state, partialTicks);
        state.helm = crab.hasHelm();
    }
}
