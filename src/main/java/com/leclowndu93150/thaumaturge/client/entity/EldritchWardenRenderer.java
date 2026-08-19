package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGuardianModel;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchWarden;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public final class EldritchWardenRenderer extends MobRenderer<EntityEldritchWarden, EldritchWardenRenderer.State, EldritchGuardianModel> {
    public static final class State extends EldritchGuardianRenderState {
        public float spawnFraction;
        public float height;
    }

    private static final Identifier TEXTURE = TCIds.rl("textures/entity/eldritch_warden.png");
    private static final float SHADOW = 0.5F;
    private static final float SPAWN_TICKS = 150.0F;

    public EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel(context.bakeLayer(TCModelLayers.ELDRITCH_GUARDIAN)), SHADOW);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityEldritchWarden entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.armLiftL = entity.armLiftL;
        state.armLiftR = entity.armLiftR;
        state.alpha = 1.0F;
        state.spawnFraction = entity.getSpawnTimer() / SPAWN_TICKS;
        state.height = entity.getBbHeight();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        if (state.spawnFraction > 0.0F) {
            poseStack.translate(0.0F, -state.height * state.spawnFraction, 0.0F);
        }
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }
}
