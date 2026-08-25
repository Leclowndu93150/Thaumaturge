package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class OccludingEffectRenderer {
    private static final ResourceLocation WISPY_TEXTURE = TCIds.rl("textures/misc/wispy.png");
    private static final ResourceLocation PORTAL_TEXTURE = TCIds.rl("textures/misc/cultist_portal.png");

    private static final RenderType WISPY = TCRenderTypes.occludingEffect(WISPY_TEXTURE);
    private static final RenderType PORTAL = TCRenderTypes.occludingEffect(PORTAL_TEXTURE);

    private static final List<Beam> BEAMS = new ArrayList<>();
    private static final List<Portal> PORTALS = new ArrayList<>();

    private record Beam(
            Vec3 origin, Vec3 fromRelative, float time, int color, float speed, float distanceFraction, float width) {}

    private record Portal(Vec3 origin, float scaleX, float scaleY, float u0, float u1, int tint, int light) {}

    private OccludingEffectRenderer() {}

    public static void enqueueBeam(
            Vec3 origin, Vec3 fromRelative, float time, int color, float speed, float distanceFraction, float width) {
        BEAMS.add(new Beam(origin, fromRelative, time, color, speed, distanceFraction, width));
    }

    public static void enqueuePortal(Vec3 origin, float scaleX, float scaleY, float u0, float u1, int tint, int light) {
        PORTALS.add(new Portal(origin, scaleX, scaleY, u0, u1, tint, light));
    }

    static void render(RenderLevelStageEvent event) {
        if (BEAMS.isEmpty() && PORTALS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        try {
            renderBeams(poseStack, buffers, camera, WISPY);
            renderPortals(poseStack, buffers, camera, PORTAL);
        } finally {
            BEAMS.clear();
            PORTALS.clear();
        }
    }

    private static void renderBeams(
            PoseStack poseStack, MultiBufferSource.BufferSource buffers, Vec3 camera, RenderType renderType) {
        if (BEAMS.isEmpty()) {
            return;
        }
        VertexConsumer buffer = buffers.getBuffer(renderType);
        for (Beam beam : BEAMS) {
            poseStack.pushPose();
            poseStack.translate(beam.origin.x - camera.x, beam.origin.y - camera.y, beam.origin.z - camera.z);
            FloatyLineRenderer.write(
                    poseStack.last(),
                    buffer,
                    beam.fromRelative,
                    beam.time,
                    beam.color,
                    beam.speed,
                    beam.distanceFraction,
                    beam.width);
            poseStack.popPose();
        }
        buffers.endBatch(renderType);
    }

    private static void renderPortals(
            PoseStack poseStack, MultiBufferSource.BufferSource buffers, Vec3 camera, RenderType renderType) {
        if (PORTALS.isEmpty()) {
            return;
        }
        VertexConsumer buffer = buffers.getBuffer(renderType);
        for (Portal portal : PORTALS) {
            poseStack.pushPose();
            poseStack.translate(portal.origin.x - camera.x, portal.origin.y - camera.y, portal.origin.z - camera.z);
            poseStack.mulPose(
                    Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            writePortal(poseStack.last(), buffer, portal);
            poseStack.popPose();
        }
        buffers.endBatch(renderType);
    }

    private static void writePortal(PoseStack.Pose pose, VertexConsumer buffer, Portal portal) {
        Matrix4f matrix = pose.pose();
        portalVertex(pose, buffer, matrix, -portal.scaleX, -portal.scaleY, portal.u1, 0.0F, portal);
        portalVertex(pose, buffer, matrix, -portal.scaleX, portal.scaleY, portal.u1, 1.0F, portal);
        portalVertex(pose, buffer, matrix, portal.scaleX, portal.scaleY, portal.u0, 1.0F, portal);
        portalVertex(pose, buffer, matrix, portal.scaleX, -portal.scaleY, portal.u0, 0.0F, portal);
    }

    private static void portalVertex(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            Matrix4f matrix,
            float x,
            float y,
            float u,
            float v,
            Portal portal) {
        buffer.addVertex(matrix, x, y, 0.0F)
                .setUv(u, v)
                .setColor(portal.tint)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(portal.light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
