package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistCleric;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CultistClericRenderer extends HumanoidMobRenderer<EntityCultistCleric, CultistClericRenderer.State, HumanoidModel<CultistClericRenderer.State>> {
    public static final class State extends HumanoidRenderState {
        public boolean ritualist;
        public float bob;
        public float lineStartY;
        public Vec3 lineTo = Vec3.ZERO;
        public float time;
        public float lineFade;
    }

    private static final Identifier TEXTURE = TCIds.rl("textures/entity/cultist.png");
    private static final float SHADOW = 0.5F;
    private static final int BOB_PHASE_RANGE = 1000;
    private static final float BOB_PERIOD = 9.0F;
    private static final float BOB_AMPLITUDE = 0.1F;
    private static final float BOB_BASE = 0.21F;
    private static final float LINE_START_EYE_SCALE = 1.2F;
    private static final double LINE_END_HEIGHT = 1.5;
    private static final int LINE_COLOR = 0x110011;
    private static final float LINE_SPEED = -0.03F;
    private static final float LINE_WIDTH = 0.25F;
    private static final float LINE_FADE_IN_TICKS = 10.0F;

    public CultistClericRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), SHADOW);
        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new), context.getEquipmentRenderer()));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityCultistCleric entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.ritualist = entity.isRitualist();
        if (!state.ritualist) {
            return;
        }
        int phase = Math.floorMod(entity.getId(), BOB_PHASE_RANGE);
        float cycle = entity.tickCount + partialTicks + phase;
        state.bob = Mth.sin(cycle / BOB_PERIOD) * BOB_AMPLITUDE + BOB_BASE;
        state.lineStartY = entity.getEyeHeight() * LINE_START_EYE_SCALE;
        BlockPos anchor = entity.ritualAnchor();
        Vec3 position = entity.getPosition(partialTicks);
        state.lineTo = new Vec3(anchor.getX() + 0.5 - position.x, anchor.getY() + LINE_END_HEIGHT - state.bob - (position.y + state.lineStartY), anchor.getZ() + 0.5 - position.z);
        state.time = FloatyLineRenderer.time(entity.level().getGameTime(), partialTicks);
        state.lineFade = Math.min(entity.tickCount, LINE_FADE_IN_TICKS) / LINE_FADE_IN_TICKS;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.ritualist) {
            super.submit(state, poseStack, collector, camera);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, state.bob, 0.0F);
        super.submit(state, poseStack, collector, camera);
        poseStack.translate(0.0F, state.lineStartY, 0.0F);
        FloatyLineRenderer.submit(poseStack, collector, state.lineTo, state.time, LINE_COLOR, LINE_SPEED, state.lineFade, LINE_WIDTH);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }
}
