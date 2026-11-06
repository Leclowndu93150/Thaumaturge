package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class LateWorldRenderQueue {
    public interface LateDraw {
        void draw(PoseStack poseStack, MultiBufferSource buffers);
    }

    public interface OwnShaderGeometry {
        void write(PoseStack.Pose pose, VertexConsumer buffer);
    }

    private enum Source {
        ENTITY,
        BLOCK_ENTITY,
        BLOCK_ENTITY_OVERLAY
    }

    private record Entry(Vec3 origin, Source source, LateDraw draw) {}

    private record OwnShaderDraw(Matrix4f pose, Matrix3f normal, RenderType type, OwnShaderGeometry geometry) {}

    private static final List<Entry> QUEUE = new ArrayList<>();
    private static final List<Entry> AFTER_BLOCK_ENTITIES = new ArrayList<>();
    private static final List<Entry> AFTER_LEVEL = new ArrayList<>();
    private static final List<OwnShaderDraw> OWN_SHADER = new ArrayList<>();

    private LateWorldRenderQueue() {}

    public static void clear() {
        QUEUE.clear();
        AFTER_BLOCK_ENTITIES.clear();
        AFTER_LEVEL.clear();
        OWN_SHADER.clear();
    }

    public static void enqueue(Vec3 origin, LateDraw draw) {
        enqueueEntity(origin, draw);
    }

    public static void enqueueEntity(Vec3 origin, LateDraw draw) {
        add(effectQueue(), new Entry(origin, Source.ENTITY, draw));
    }

    public static void enqueueBlockEntity(Vec3 origin, LateDraw draw) {
        add(effectQueue(), new Entry(origin, Source.BLOCK_ENTITY, draw));
    }

    public static void enqueueBlockEntityAfterGeometry(Vec3 origin, LateDraw draw) {
        add(AFTER_BLOCK_ENTITIES, new Entry(origin, Source.BLOCK_ENTITY, draw));
    }

    public static void enqueueBlockEntityAfterTranslucent(Vec3 origin, LateDraw draw) {
        add(AFTER_LEVEL, new Entry(origin, Source.BLOCK_ENTITY, draw));
    }

    public static void enqueueBlockEntityOverlay(Vec3 origin, LateDraw draw) {
        add(AFTER_LEVEL, new Entry(origin, Source.BLOCK_ENTITY_OVERLAY, draw));
    }

    public static void drawOwnShader(
            PoseStack poseStack, MultiBufferSource buffers, RenderType type, OwnShaderGeometry geometry) {
        if (!IrisCompat.shadersActive()) {
            geometry.write(poseStack.last(), buffers.getBuffer(type));
        } else if (!IrisCompat.isShadowPass()) {
            PoseStack.Pose pose = poseStack.last();
            OWN_SHADER.add(new OwnShaderDraw(new Matrix4f(pose.pose()), new Matrix3f(pose.normal()), type, geometry));
        }
    }

    private static void add(List<Entry> queue, Entry entry) {
        if (!IrisCompat.isShadowPass()) {
            queue.add(entry);
        }
    }

    private static List<Entry> effectQueue() {
        return IrisCompat.shadersActive() ? AFTER_LEVEL : QUEUE;
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            if (IrisCompat.shadersActive()) {
                OccludingEffectRenderer.render(event);
            }
            render(event, AFTER_LEVEL);
            renderOwnShader(event);
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
                render(event, AFTER_BLOCK_ENTITIES);
            }
            return;
        }
        if (!IrisCompat.shadersActive()) {
            OccludingEffectRenderer.render(event);
        }
        render(event, QUEUE);
    }

    private static void render(RenderLevelStageEvent event, List<Entry> queue) {
        if (queue.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        boolean afterLevel = event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL;
        if (afterLevel || event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            buffers.endBatch();
        }
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = renderPose(event);
        for (Entry entry : queue) {
            poseStack.pushPose();
            Vec3 origin = ClientSpaces.renderPosition(mc.level, entry.origin);
            poseStack.translate(origin.x - cam.x, origin.y - cam.y, origin.z - cam.z);
            entry.draw.draw(poseStack, afterLevel ? buffers : effectBuffers(entry.source, buffers));
            poseStack.popPose();
        }
        queue.clear();
        buffers.endBatch();
    }

    private static MultiBufferSource effectBuffers(Source source, MultiBufferSource buffers) {
        return source != Source.ENTITY
                ? IrisCompat.blockEntityEffectBuffers(buffers)
                : IrisCompat.entityEffectBuffers(buffers);
    }

    private static void renderOwnShader(RenderLevelStageEvent event) {
        if (OWN_SHADER.isEmpty()) {
            return;
        }
        MultiBufferSource.BufferSource buffers =
                Minecraft.getInstance().renderBuffers().bufferSource();
        buffers.endBatch();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.mul(event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack = new PoseStack();
        for (OwnShaderDraw draw : OWN_SHADER) {
            poseStack.pushPose();
            poseStack.last().pose().set(draw.pose);
            poseStack.last().normal().set(draw.normal);
            draw.geometry.write(poseStack.last(), buffers.getBuffer(draw.type));
            poseStack.popPose();
        }
        OWN_SHADER.clear();
        buffers.endBatch();
        modelView.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    static PoseStack renderPose(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return event.getPoseStack();
        }
        // AFTER_LEVEL has an empty pose stack and no longer has the world's model-view state.
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(event.getModelViewMatrix());
        return poseStack;
    }
}
