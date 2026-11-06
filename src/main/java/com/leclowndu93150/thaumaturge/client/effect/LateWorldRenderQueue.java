package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class LateWorldRenderQueue {
    public interface LateDraw {
        void draw(PoseStack poseStack, MultiBufferSource buffers);
    }

    private record Entry(Vec3 origin, LateDraw draw) {
    }

    private record Geometry(PoseStack.Pose pose, RenderType type, SubmitNodeCollector.CustomGeometryRenderer renderer) {
    }

    private static final List<Entry> QUEUE = new ArrayList<>();
    private static final List<Geometry> AFTER_LEVEL = new ArrayList<>();

    private LateWorldRenderQueue() {}

    public static void clear() {
        QUEUE.clear();
        AFTER_LEVEL.clear();
    }

    public static void enqueue(Vec3 origin, LateDraw draw) {
        QUEUE.add(new Entry(origin, draw));
    }

    private static void enqueueAfterLevel(PoseStack poseStack, RenderType type, SubmitNodeCollector.CustomGeometryRenderer renderer) {
        AFTER_LEVEL.add(new Geometry(poseStack.last().copy(), type, renderer));
    }

    public static void submitOwnShader(PoseStack poseStack, SubmitNodeCollector collector, RenderType type, SubmitNodeCollector.CustomGeometryRenderer renderer) {
        if (!IrisCompat.shaderPackInUse()) {
            collector.submitCustomGeometry(poseStack, type, renderer);
        } else if (!IrisCompat.isShadowPass()) {
            enqueueAfterLevel(poseStack, type, renderer);
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        if (QUEUE.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        PoseStack poseStack = event.getPoseStack();
        for (Entry entry : QUEUE) {
            poseStack.pushPose();
            poseStack.translate(entry.origin.x - cam.x, entry.origin.y - cam.y, entry.origin.z - cam.z);
            entry.draw.draw(poseStack, buffers);
            poseStack.popPose();
        }
        QUEUE.clear();
        buffers.endBatch();
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        if (AFTER_LEVEL.isEmpty()) {
            return;
        }
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.mul(event.getModelViewMatrix());
        for (Geometry geometry : AFTER_LEVEL) {
            geometry.renderer().render(geometry.pose(), buffers.getBuffer(geometry.type()));
        }
        AFTER_LEVEL.clear();
        buffers.endBatch();
        modelView.popMatrix();
    }
}
