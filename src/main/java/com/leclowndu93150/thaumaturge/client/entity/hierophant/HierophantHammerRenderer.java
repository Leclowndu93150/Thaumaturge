package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantHammer;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantAction;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantSockets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public final class HierophantHammerRenderer extends EntityRenderer<EntityHierophantHammer> {
    public static final class State {
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

    private void extractRenderState(EntityHierophantHammer entity, State state, float partialTick) {
        state.age = entity.age(partialTick);
        state.casterYaw = entity.casterYaw();
        state.displacement = entity.center(state.age).subtract(entity.position());
        state.pose.action = HierophantAction.THROW;
        state.pose.actionTicks = HierophantAction.THROW.release();
    }

    @Override
    public void render(
            EntityHierophantHammer entity,
            float yaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource collector,
            int light) {
        super.render(entity, yaw, partialTick, pose, collector, light);
        State state = new State();
        extractRenderState(entity, state, partialTick);
        final Vec3 palm =
                HierophantSockets.release(HierophantAction.THROW, false).scale(1 / UNITS_PER_BLOCK);
        pose.pushPose();
        pose.translate(state.displacement.x, state.displacement.y, state.displacement.z);
        pose.mulPose(Axis.YP.rotationDegrees(180 - state.casterYaw));
        pose.scale(
                -EntityEldritchHierophant.MODEL_SCALE,
                -EntityEldritchHierophant.MODEL_SCALE,
                EntityEldritchHierophant.MODEL_SCALE);
        pose.mulPose(Axis.XP.rotationDegrees(state.age * SPIN_PER_TICK));
        pose.translate(palm.x, palm.y + MODEL_BASE_OFFSET, -palm.z);
        model.setupAnim(state.pose);
        model.renderToBuffer(pose, collector.getBuffer(HierophantRenderTypes.HAMMER), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    @Override
    public boolean shouldRender(EntityHierophantHammer entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z)
                && frustum.isVisible(entity.getBoundingBox().inflate(CULLING_MARGIN));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityHierophantHammer entity) {
        return HierophantTextures.SPELLS;
    }
}
