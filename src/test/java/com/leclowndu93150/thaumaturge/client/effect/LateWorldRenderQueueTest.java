package com.leclowndu93150.thaumaturge.client.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LateWorldRenderQueueTest {
    @AfterEach
    void clearQueues() {
        LateWorldRenderQueue.clear();
    }

    @Test
    void nodeLayersRenderAfterTheLevelWithAndWithoutShaders() throws Exception {
        Minecraft minecraft = mock(Minecraft.class);
        GameRenderer renderer = mock(GameRenderer.class);
        Field field = Minecraft.class.getField("gameRenderer");
        field.setAccessible(true);
        field.set(minecraft, renderer);
        Camera camera = mock(Camera.class);
        when(renderer.getMainCamera()).thenReturn(camera);
        when(camera.getPosition()).thenReturn(Vec3.ZERO);
        RenderBuffers renderBuffers = mock(RenderBuffers.class);
        MultiBufferSource.BufferSource buffers = mock(MultiBufferSource.BufferSource.class);
        when(minecraft.renderBuffers()).thenReturn(renderBuffers);
        when(renderBuffers.bufferSource()).thenReturn(buffers);
        RenderTarget target = mock(RenderTarget.class);
        when(minecraft.getMainRenderTarget()).thenReturn(target);
        List<String> draws = new ArrayList<>();

        try (var client = mockStatic(Minecraft.class);
                var iris = mockStatic(IrisCompat.class);
                var sable = mockStatic(ClientSpaces.class)) {
            client.when(Minecraft::getInstance).thenReturn(minecraft);
            iris.when(() -> IrisCompat.blockEntityEffectBuffers(buffers)).thenReturn(buffers);
            sable.when(() -> ClientSpaces.renderPosition(null, Vec3.ZERO)).thenReturn(Vec3.ZERO);
            iris.when(IrisCompat::shadersActive).thenReturn(false);
            LateWorldRenderQueue.enqueueBlockEntityAfterTranslucent(Vec3.ZERO, (pose, source) -> draws.add("vanilla"));
            LateWorldRenderQueue.enqueueBlockEntityOverlay(Vec3.ZERO, (pose, source) -> draws.add("node"));

            LateWorldRenderQueue.onRender(event(RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES));
            assertTrue(draws.isEmpty());
            LateWorldRenderQueue.onRender(event(RenderLevelStageEvent.Stage.AFTER_PARTICLES));
            assertTrue(draws.isEmpty());
            LateWorldRenderQueue.onRender(event(RenderLevelStageEvent.Stage.AFTER_LEVEL));
            assertEquals(List.of("vanilla", "node"), draws);

            iris.when(IrisCompat::shadersActive).thenReturn(true);
            LateWorldRenderQueue.enqueueBlockEntityAfterTranslucent(Vec3.ZERO, (pose, source) -> draws.add("iris"));
            LateWorldRenderQueue.onRender(event(RenderLevelStageEvent.Stage.AFTER_PARTICLES));
            assertEquals(List.of("vanilla", "node"), draws);
            LateWorldRenderQueue.onRender(event(RenderLevelStageEvent.Stage.AFTER_LEVEL));
            assertEquals(List.of("vanilla", "node", "iris"), draws);
        }
    }

    @Test
    void finalOverlayRestoresWorldViewWithoutChangingTheEventPose() {
        RenderLevelStageEvent event = event(RenderLevelStageEvent.Stage.AFTER_LEVEL);
        Matrix4f view = new Matrix4f().rotationY(0.7F).rotateX(-0.2F);
        when(event.getModelViewMatrix()).thenReturn(view);
        PoseStack pose = LateWorldRenderQueue.renderPose(event);
        Vec3 relative = new Vec3(3, 4, 5);
        pose.translate(relative.x, relative.y, relative.z);
        pose.mulPose(view.getUnnormalizedRotation(new Quaternionf()).conjugate());

        Vector3f centre = pose.last().pose().transformPosition(new Vector3f());
        Vector3f expected = view.transformPosition(new Vector3f(3, 4, 5));
        assertEquals(expected.x, centre.x, 0.00001F);
        assertEquals(expected.y, centre.y, 0.00001F);
        assertEquals(expected.z, centre.z, 0.00001F);
        assertEquals(1.0F, pose.last().pose().m00(), 0.00001F);
        assertEquals(1.0F, pose.last().pose().m11(), 0.00001F);
        assertEquals(1.0F, pose.last().pose().m22(), 0.00001F);
        assertTrue(event.getPoseStack().last().pose().equals(new Matrix4f()));
    }

    private static RenderLevelStageEvent event(RenderLevelStageEvent.Stage stage) {
        RenderLevelStageEvent event = mock(RenderLevelStageEvent.class);
        when(event.getStage()).thenReturn(stage);
        when(event.getPoseStack()).thenReturn(new PoseStack());
        when(event.getModelViewMatrix()).thenReturn(new Matrix4f());
        return event;
    }
}
