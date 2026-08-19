package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public final class CultistPortalGreaterRenderer extends EntityRenderer<EntityCultistPortalGreater, CultistPortalRenderer.State> {
    private static final float BASE_SCALE_Y = 1.5F;
    private static final float SCALE_FACTOR = 1.3F;
    private static final float SHADOW = 0.1F;

    public CultistPortalGreaterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW;
    }

    @Override
    public CultistPortalRenderer.State createRenderState() {
        return new CultistPortalRenderer.State();
    }

    @Override
    public void extractRenderState(EntityCultistPortalGreater entity, CultistPortalRenderer.State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.active = true;
        state.activeCounter = entity.tickCount + partialTicks;
        state.hurtTime = entity.hurtTime;
        state.pulse = entity.pulse;
        state.healthFraction = entity.getHealth() / entity.getMaxHealth();
        state.halfHeight = entity.getBbHeight() / 2.0F;
    }

    @Override
    public void submit(CultistPortalRenderer.State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        CultistPortalRenderer.submitPortal(state, poseStack, collector, camera, BASE_SCALE_Y, SCALE_FACTOR);
    }
}
