package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class EldritchPortalSurface {
    public static final RenderType SURFACE = RenderType.create("tc_eldritch_portal_surface", RenderSetup.builder(TTRenderPipelines.PORTAL_SURFACE).createRenderSetup());

    private EldritchPortalSurface() {}

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, SubmitNodeCollector.CustomGeometryRenderer geometry) {
        LateWorldRenderQueue.submitOwnShader(poseStack, collector, SURFACE, geometry);
    }

    public static void quad(PoseStack.Pose pose, VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        buffer.addVertex(pose, x1, y1, z1);
        buffer.addVertex(pose, x2, y2, z2);
        buffer.addVertex(pose, x3, y3, z3);
        buffer.addVertex(pose, x4, y4, z4);
    }
}
