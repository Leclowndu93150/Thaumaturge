package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGolemModel;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchGolem;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class EldritchGolemRenderer extends ScaledMobRenderer<EntityEldritchGolem, EldritchGolemRenderState, EldritchGolemModel> {
    private static final float GOLEM_SIZE = 1.8F;
    private static final float GOLEM_SHADOW = 0.7F;

    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGolemModel(context.bakeLayer(TCModelLayers.ELDRITCH_GOLEM)), TCIds.rl("textures/entity/eldritch_golem.png"), GOLEM_SIZE, GOLEM_SHADOW);
    }

    @Override
    public EldritchGolemRenderState createRenderState() {
        return new EldritchGolemRenderState();
    }

    @Override
    public void extractRenderState(EntityEldritchGolem golem, EldritchGolemRenderState state, float partialTicks) {
        super.extractRenderState(golem, state, partialTicks);
        state.headless = golem.isHeadless();
        state.attackTime = Math.max(0.0F, golem.swingCooldown() - partialTicks);
        state.spawnTimer = golem.getSpawnTimer();
    }
}
