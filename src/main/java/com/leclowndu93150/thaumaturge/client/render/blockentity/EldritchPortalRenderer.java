package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchPortal;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class EldritchPortalRenderer implements BlockEntityRenderer<BlockEntityEldritchPortal, EldritchPortalRenderState> {
    private static final Identifier TEXTURE = TTIds.rl("textures/misc/eldritch_portal.png");
    private static final Identifier OVERWORLD_TEXTURE = TTIds.rl("textures/misc/eldritch_portal_overworld.png");
    private static final RenderType PORTAL_TYPE = RenderType.create("tc_eldritch_portal",
            RenderSetup.builder(TTRenderPipelines.FX_TRANSLUCENT).withTexture("Sampler0", TEXTURE).useLightmap().createRenderSetup());
    private static final RenderType OVERWORLD_PORTAL_TYPE = RenderType.create("tc_eldritch_portal_overworld",
            RenderSetup.builder(TTRenderPipelines.FX_TRANSLUCENT).withTexture("Sampler0", OVERWORLD_TEXTURE).useLightmap().createRenderSetup());

    private static final int FRAMES = 32;
    private static final float FRAME_WIDTH = 1.0F / FRAMES;
    private static final float GROW_TICKS_WIDTH = 5.0F;
    private static final float GROW_TICKS_HEIGHT = 30.0F;
    private static final int LIGHT = 0x00F000DC;

    public EldritchPortalRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public EldritchPortalRenderState createRenderState() {
        return new EldritchPortalRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEldritchPortal portal, EldritchPortalRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(portal, state, partialTicks, cameraPosition, breakProgress);
        state.openCount = portal.opencount + partialTicks;
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        state.animationTime = viewEntity == null ? partialTicks : viewEntity.tickCount + partialTicks;
        state.towardOverworld = portal.getLevel() != null && portal.getLevel().dimension() == OuterLands.DIMENSION;
    }

    @Override
    public void submit(EldritchPortalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.openCount < 0.0F) {
            return;
        }
        float sx = Math.min(GROW_TICKS_WIDTH, state.openCount) / GROW_TICKS_WIDTH;
        float sy = Math.min(GROW_TICKS_HEIGHT, state.openCount) / GROW_TICKS_HEIGHT;
        int frame = (int) state.animationTime % FRAMES;
        float u0 = frame * FRAME_WIDTH;
        float u1 = u0 + FRAME_WIDTH;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, state.towardOverworld ? OVERWORLD_PORTAL_TYPE : PORTAL_TYPE, (pose, buffer) -> {
            Matrix4fc mat = pose.pose();
            buffer.addVertex(mat, -sx, -sy, 0.0F).setUv(u1, 0.0F).setColor(-1).setLight(LIGHT);
            buffer.addVertex(mat, -sx, sy, 0.0F).setUv(u1, 1.0F).setColor(-1).setLight(LIGHT);
            buffer.addVertex(mat, sx, sy, 0.0F).setUv(u0, 1.0F).setColor(-1).setLight(LIGHT);
            buffer.addVertex(mat, sx, -sy, 0.0F).setUv(u0, 0.0F).setColor(-1).setLight(LIGHT);
        });
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityEldritchPortal portal) {
        return new AABB(portal.getBlockPos()).inflate(1.5);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
