package com.leclowndu93150.thaumaturge.client.render.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

final class AspectTagWorldRendererAmountsTest {
    @Test
    void reservoirSideIconsAndAmountsStayCenteredOutsideTheBlockDuringAnimation() throws Exception {
        Minecraft minecraft = mock(Minecraft.class);
        Camera camera = mock(Camera.class);
        GameRenderer gameRenderer = mock(GameRenderer.class);
        when(gameRenderer.getMainCamera()).thenReturn(camera);
        setMinecraftField(minecraft, "gameRenderer", gameRenderer);
        Font font = mock(Font.class);
        setMinecraftField(minecraft, "font", font);
        MultiBufferSource.BufferSource buffers = mock(MultiBufferSource.BufferSource.class);
        RenderBuffers renderBuffers = mock(RenderBuffers.class);
        when(renderBuffers.bufferSource()).thenReturn(buffers);
        when(minecraft.renderBuffers()).thenReturn(renderBuffers);

        ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/test.png");
        IAspect aer = mock(IAspect.class);
        when(aer.tag()).thenReturn("aer");
        when(aer.texture()).thenReturn(texture);
        IAspect ignis = mock(IAspect.class);
        when(ignis.tag()).thenReturn("ignis");
        when(ignis.texture()).thenReturn(texture);
        AspectList aspects =
                AspectList.of(new AspectInstance(Holder.direct(aer), 7), new AspectInstance(Holder.direct(ignis), 11));
        RenderType renderType = mock(RenderType.class);
        VertexConsumer consumer = mock(VertexConsumer.class, invocation -> invocation.getMock());
        when(buffers.getBuffer(renderType)).thenReturn(consumer);
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> amounts = new ArrayList<>();
        doAnswer(invocation -> {
                    PoseStack.Pose pose = invocation.getArgument(0);
                    vertices.add(new Vector3f(
                                    invocation.getArgument(1, Float.class),
                                    invocation.getArgument(2, Float.class),
                                    invocation.getArgument(3, Float.class))
                            .mulPosition(pose.pose()));
                    return consumer;
                })
                .when(consumer)
                .addVertex(any(PoseStack.Pose.class), anyFloat(), anyFloat(), anyFloat());
        doAnswer(invocation -> {
                    amounts.add(new Vector3f().mulPosition(invocation.getArgument(5, Matrix4f.class)));
                    return 0;
                })
                .when(font)
                .drawInBatch(
                        anyString(),
                        anyFloat(),
                        anyFloat(),
                        anyInt(),
                        anyBoolean(),
                        any(),
                        eq(buffers),
                        eq(Font.DisplayMode.NORMAL),
                        anyInt(),
                        anyInt());

        try (MockedStatic<TTFlatRenderTypes> renderTypes = mockStatic(TTFlatRenderTypes.class);
                MockedStatic<IrisCompat> iris = mockStatic(IrisCompat.class)) {
            renderTypes
                    .when(() -> TTFlatRenderTypes.entityTranslucentFlat(texture))
                    .thenReturn(renderType);
            iris.when(() -> IrisCompat.entityEffectBuffers(buffers)).thenReturn(buffers);
            for (Vec3 cameraPos : List.of(new Vec3(4.5, 0.5, 0.5), new Vec3(4.5, 0.5, 4.5), new Vec3(-3.5, 0.5, 4.5))) {
                when(camera.getPosition()).thenReturn(cameraPos);
                Direction side = Direction.getNearest((float) (cameraPos.x - 0.5), 0.0F, (float) (cameraPos.z - 0.5));
                Vec3 normal = Vec3.atLowerCornerOf(side.getNormal());
                double blockExtent = 0.5;
                for (float scale : new float[] {0.05F, 0.15F, 0.3F}) {
                    vertices.clear();
                    amounts.clear();
                    AspectTagWorldRenderer.renderTagCloud(
                            new PoseStack(),
                            minecraft,
                            0.0,
                            0.0,
                            0.0,
                            aspects,
                            side,
                            scale,
                            0.75F,
                            aspect -> true,
                            true,
                            0.0F,
                            1.2F,
                            true);
                    assertEquals(8, vertices.size());
                    assertEquals(4, amounts.size());
                    for (Vector3f point : vertices) {
                        assertOutsideBlock(point, cameraPos, normal, blockExtent);
                    }
                    for (Vector3f point : amounts) {
                        assertOutsideBlock(point, cameraPos, normal, blockExtent);
                    }
                    double centerX = vertices.stream()
                                    .mapToDouble(point -> point.x)
                                    .average()
                                    .orElseThrow()
                            + cameraPos.x
                            - 0.5;
                    double centerZ = vertices.stream()
                                    .mapToDouble(point -> point.z)
                                    .average()
                                    .orElseThrow()
                            + cameraPos.z
                            - 0.5;
                    assertEquals(side.getStepX(), centerX, 0.00001, "Icons must occupy the adjacent block's center X");
                    assertEquals(side.getStepZ(), centerZ, 0.00001, "Icons must occupy the adjacent block's center Z");
                }
            }
        }
    }

    private static void assertOutsideBlock(Vector3f point, Vec3 camera, Vec3 normal, double blockExtent) {
        double distance = (point.x + camera.x - 0.5) * normal.x + (point.z + camera.z - 0.5) * normal.z;
        assertTrue(distance > blockExtent + 0.01, "The side readout must clear the reservoir's block volume");
    }

    @Test
    void queryOverloadOmitsAmountTextWhileLegacyOverloadsRetainIt() throws Exception {
        Minecraft minecraft = mock(Minecraft.class);
        Camera camera = mock(Camera.class);
        when(camera.getPosition()).thenReturn(Vec3.ZERO);
        GameRenderer gameRenderer = mock(GameRenderer.class);
        when(gameRenderer.getMainCamera()).thenReturn(camera);
        setMinecraftField(minecraft, "gameRenderer", gameRenderer);

        Font font = mock(Font.class);
        when(font.width("7")).thenReturn(1);
        setMinecraftField(minecraft, "font", font);
        MultiBufferSource.BufferSource buffers = mock(MultiBufferSource.BufferSource.class);
        RenderBuffers renderBuffers = mock(RenderBuffers.class);
        when(renderBuffers.bufferSource()).thenReturn(buffers);
        when(minecraft.renderBuffers()).thenReturn(renderBuffers);

        IAspect aspect = mock(IAspect.class);
        ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/test.png");
        when(aspect.texture()).thenReturn(texture);
        Holder<IAspect> holder = Holder.direct(aspect);
        AspectList aspects = AspectList.of(new AspectInstance(holder, 7));
        RenderType renderType = mock(RenderType.class);
        VertexConsumer vertexConsumer = mock(VertexConsumer.class, invocation -> invocation.getMock());
        when(buffers.getBuffer(renderType)).thenReturn(vertexConsumer);

        try (MockedStatic<TTFlatRenderTypes> renderTypes = mockStatic(TTFlatRenderTypes.class);
                MockedStatic<IrisCompat> iris = mockStatic(IrisCompat.class)) {
            renderTypes
                    .when(() -> TTFlatRenderTypes.entityTranslucentFlat(texture))
                    .thenReturn(renderType);
            iris.when(() -> IrisCompat.entityEffectBuffers(buffers)).thenReturn(buffers);

            AspectTagWorldRenderer.renderTagCloud(
                    new PoseStack(), minecraft, 0.0, 0.0, 0.0, aspects, Direction.UP, 0.3F, 0.75F);
            verifyAmountDraws(font, buffers, 2);

            clearInvocations(font);
            AspectTagWorldRenderer.renderTagCloud(
                    new PoseStack(), minecraft, 0.0, 0.0, 0.0, aspects, Direction.UP, 0.3F, 0.75F, entry -> true);
            verifyAmountDraws(font, buffers, 2);

            clearInvocations(font);
            AspectTagWorldRenderer.renderTagCloud(
                    new PoseStack(),
                    minecraft,
                    0.0,
                    0.0,
                    0.0,
                    aspects,
                    Direction.UP,
                    0.3F,
                    0.75F,
                    entry -> true,
                    false);
            verify(font, times(0))
                    .drawInBatch(
                            anyString(),
                            anyFloat(),
                            anyFloat(),
                            anyInt(),
                            anyBoolean(),
                            any(),
                            eq(buffers),
                            eq(Font.DisplayMode.NORMAL),
                            anyInt(),
                            anyInt());
        }
    }

    private static void verifyAmountDraws(Font font, MultiBufferSource.BufferSource buffers, int count) {
        verify(font, times(count))
                .drawInBatch(
                        anyString(),
                        anyFloat(),
                        anyFloat(),
                        anyInt(),
                        anyBoolean(),
                        any(),
                        eq(buffers),
                        eq(Font.DisplayMode.NORMAL),
                        anyInt(),
                        anyInt());
    }

    private static void setMinecraftField(Minecraft minecraft, String name, Object value) throws Exception {
        java.lang.reflect.Field field = Minecraft.class.getField(name);
        field.setAccessible(true);
        field.set(minecraft, value);
    }
}
