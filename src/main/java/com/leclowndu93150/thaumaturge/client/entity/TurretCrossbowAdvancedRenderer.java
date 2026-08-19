package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbowAdvanced;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public final class TurretCrossbowAdvancedRenderer extends EntityRenderer<EntityTurretCrossbowAdvanced, TurretCrossbowAdvancedRenderer.State> {
    public static final class State extends TurretCrossbowRenderState {
        public float headYaw;
        public float headPitch;
    }

    private static final Identifier MODEL = TCIds.rl("models/mesh/crossbow_advanced.tcmesh");
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/crossbow_advanced.png");
    private static final float BASE_LIFT = 0.75F;
    private static final float SHADOW = 0.5F;
    private static final float HURT_JIGGLE_DIVISOR = 500.0F;
    private static final int HURT_TINT = ARGB.colorFromFloat(1.0F, 1.0F, 0.5F, 0.5F);

    private final RandomSource jiggleRandom = RandomSource.create();

    public TurretCrossbowAdvancedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityTurretCrossbowAdvanced entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.swingAnim = entity.swingAnim;
        state.loadProgress = entity.getLoadProgress(partialTicks);
        state.ridingMinecart = entity.getVehicle() instanceof AbstractMinecart;
        state.hurtTime = entity.hurtTime;
        state.headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        state.headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        TCMesh mesh = GolemMeshes.get(MODEL);
        int color = -1;
        poseStack.pushPose();
        poseStack.translate(0.0F, BASE_LIFT, 0.0F);
        poseStack.pushPose();
        if (state.ridingMinecart) {
            poseStack.scale(0.66F, 0.75F, 0.66F);
        }
        submitPart(mesh, "legs", poseStack, collector, color, state);
        poseStack.popPose();
        poseStack.pushPose();
        if (state.hurtTime > 0) {
            color = HURT_TINT;
            float jiggle = state.hurtTime / HURT_JIGGLE_DIVISOR;
            poseStack.translate(jiggleRandom.nextGaussian() * jiggle, jiggleRandom.nextGaussian() * jiggle, jiggleRandom.nextGaussian() * jiggle);
        }
        poseStack.mulPose(Axis.YN.rotationDegrees(state.headYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.headPitch));
        submitPart(mesh, "mech", poseStack, collector, color, state);
        submitPart(mesh, "box", poseStack, collector, color, state);
        submitPart(mesh, "shield", poseStack, collector, color, state);
        submitPart(mesh, "brain", poseStack, collector, color, state);
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, Mth.sin(Mth.sqrt(state.loadProgress) * Mth.TWO_PI) / 12.0F);
        submitPart(mesh, "loader", poseStack, collector, color, state);
        poseStack.popPose();
        float bowSwing = Mth.sin(Mth.sqrt(state.swingAnim) * Mth.TWO_PI) * 20.0F;
        poseStack.translate(0.0, 0.0, 0.375);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(bowSwing));
        submitPart(mesh, "bow1", poseStack, collector, color, state);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(bowSwing));
        submitPart(mesh, "bow2", poseStack, collector, color, state);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void submitPart(TCMesh mesh, String name, PoseStack poseStack, SubmitNodeCollector collector, int color, State state) {
        RenderType type = RenderTypes.entityCutout(TEXTURE);
        int light = state.lightCoords;
        for (TCMeshPart part : mesh.parts()) {
            if (name.equals(part.name())) {
                collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, color));
            }
        }
    }
}
