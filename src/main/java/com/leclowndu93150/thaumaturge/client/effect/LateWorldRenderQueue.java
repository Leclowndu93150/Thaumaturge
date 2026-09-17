package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class LateWorldRenderQueue {
    public interface LateDraw {
        void draw(PoseStack poseStack, MultiBufferSource buffers);
    }

    private enum Source {
        ENTITY,
        BLOCK_ENTITY,
        BLOCK_ENTITY_OVERLAY
    }

    private record Entry(Vec3 origin, Source source, LateDraw draw) {}

    private static final List<Entry> QUEUE = new ArrayList<>();
    private static final List<Entry> AFTER_BLOCK_ENTITIES = new ArrayList<>();

    private LateWorldRenderQueue() {}

    public static void enqueue(Vec3 origin, LateDraw draw) {
        enqueueEntity(origin, draw);
    }

    public static void enqueueEntity(Vec3 origin, LateDraw draw) {
        QUEUE.add(new Entry(origin, Source.ENTITY, draw));
    }

    public static void enqueueBlockEntity(Vec3 origin, LateDraw draw) {
        QUEUE.add(new Entry(origin, Source.BLOCK_ENTITY, draw));
    }

    public static void enqueueBlockEntityAfterGeometry(Vec3 origin, LateDraw draw) {
        AFTER_BLOCK_ENTITIES.add(new Entry(origin, Source.BLOCK_ENTITY, draw));
    }

    public static void enqueueBlockEntityOverlay(Vec3 origin, LateDraw draw) {
        AFTER_BLOCK_ENTITIES.add(new Entry(origin, Source.BLOCK_ENTITY_OVERLAY, draw));
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
                render(event, AFTER_BLOCK_ENTITIES);
            }
            return;
        }
        OccludingEffectRenderer.render(event);
        render(event, QUEUE);
    }

    private static void render(RenderLevelStageEvent event, List<Entry> queue) {
        if (queue.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            // Banner cloth and other fixed buffers must finish before the see-through overlay.
            buffers.endBatch();
        }
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        for (Entry entry : queue) {
            poseStack.pushPose();
            poseStack.translate(entry.origin.x - cam.x, entry.origin.y - cam.y, entry.origin.z - cam.z);
            MultiBufferSource effectBuffers = entry.source != Source.ENTITY
                    ? IrisCompat.blockEntityEffectBuffers(buffers)
                    : IrisCompat.entityEffectBuffers(buffers);
            entry.draw.draw(poseStack, effectBuffers);
            poseStack.popPose();
        }
        queue.clear();
        buffers.endBatch();
    }
}
