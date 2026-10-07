package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantHammer;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantAction;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantSockets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class HierophantHammerRenderer extends EntityRenderer<EntityHierophantHammer, HierophantHammerRenderer.State> {
    public static final class State extends EntityRenderState {
        public final HierophantRenderState pose = new HierophantRenderState();
        public Vec3 displacement = Vec3.ZERO;
        public float age;
        public float casterYaw;
    }
    private static final float SPIN_PER_TICK = 26;
    private static final double CULLING_MARGIN = 24;
    private static final double MODEL_BASE_OFFSET = -1.501;
    private static final double UNITS_PER_BLOCK = 16;
    private final HierophantModel model;

    public HierophantHammerRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new HierophantModel(context.bakeLayer(TTModelLayers.ELDRITCH_HIEROPHANT));
        model.onlyHammer();
    }
    @Override
    public State createRenderState() {
        return new State();
    }
    @Override
    public void extractRenderState(EntityHierophantHammer entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.age = entity.age(partialTick);
        state.casterYaw = entity.casterYaw();
        state.displacement = entity.center(state.age).subtract(entity.position());
        state.pose.action = HierophantAction.THROW;
        state.pose.actionTicks = HierophantAction.THROW.release();
    }
    @Override
    protected AABB getBoundingBoxForCulling(EntityHierophantHammer entity) {
        return entity.getBoundingBox().inflate(CULLING_MARGIN);
    }
    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, pose, collector, camera);
        final Vec3 palm = HierophantSockets.release(HierophantAction.THROW, false).scale(1 / UNITS_PER_BLOCK);
        pose.pushPose();
        pose.translate(state.displacement.x, state.displacement.y, state.displacement.z);
        pose.mulPose(Axis.YP.rotationDegrees(180 - state.casterYaw));
        pose.scale(-EntityEldritchHierophant.MODEL_SCALE, -EntityEldritchHierophant.MODEL_SCALE, EntityEldritchHierophant.MODEL_SCALE);
        pose.mulPose(Axis.XP.rotationDegrees(state.age * SPIN_PER_TICK));
        pose.translate(palm.x, palm.y + MODEL_BASE_OFFSET, -palm.z);
        collector.submitModel(model, state.pose, pose, HierophantRenderTypes.HAMMER, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        pose.popPose();
    }
}
