package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJarBrain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class JarBrainRenderer implements BlockEntityRenderer<BlockEntityJarBrain, JarBrainRenderState> {
    private static final Identifier TEX_BRAIN = TCIds.rl("textures/entity/brain2.png");
    private static final Identifier TEX_BRINE = TCIds.rl("textures/entity/jarbrine.png");
    private static final float BRAIN_SCALE = 0.4F;
    private static final float BRAIN_LIFT = -0.8F;
    private static final float BOB_PERIOD = 14.0F;
    private static final float BOB_AMPLITUDE = 0.03F;

    private final BrainModel brain;
    private final JarBrineModel brine;

    public JarBrainRenderer(BlockEntityRendererProvider.Context context) {
        this.brain = new BrainModel(context.bakeLayer(TCModelLayers.BRAIN));
        this.brine = new JarBrineModel(context.bakeLayer(TCModelLayers.JAR_BRINE));
    }

    @Override
    public JarBrainRenderState createRenderState() {
        return new JarBrainRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityJarBrain jar, JarBrainRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(jar, state, partialTicks, cameraPosition, breakProgress);
        float delta = jar.rota - jar.rotb;
        while (delta >= (float) Math.PI) {
            delta -= (float) (Math.PI * 2);
        }
        while (delta < -(float) Math.PI) {
            delta += (float) (Math.PI * 2);
        }
        state.yawRadians = jar.rotb + delta * partialTicks;
        float time = (Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount) + partialTicks;
        state.bobOffset = Mth.sin(time / BOB_PERIOD) * BOB_AMPLITUDE + BOB_AMPLITUDE;
    }

    @Override
    public void submit(JarBrainRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.01F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        poseStack.pushPose();
        poseStack.translate(0.0F, BRAIN_LIFT + state.bobOffset, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yawRadians * Mth.RAD_TO_DEG));
        poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        poseStack.scale(BRAIN_SCALE, BRAIN_SCALE, BRAIN_SCALE);
        collector.submitModelPart(brain.root, poseStack, RenderTypes.entityCutout(TEX_BRAIN), state.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, null);
        poseStack.popPose();

        collector.submitModelPart(brine.root, poseStack, RenderTypes.entityTranslucent(TEX_BRINE), state.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, null);
        poseStack.popPose();
    }
}
