package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.client.render.NodeTexture;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class NodeRendererLayersTest {
    private static final ResourceLocation TEXTURE = NodeTexture.TEXTURE;

    private record Layer(RenderType type, float scale, float alpha, int strip) {}

    @Test
    void nodeLayersUseTheAtlasProcessedForNodeRendering() {
        assertEquals(TTIds.rl("textures/misc/auranodes.png"), TEXTURE);
    }

    @Test
    void allNodeTypesKeepTheirSpritesBlendsAndRevealingDepthRules() {
        for (NodeType type : NodeType.values()) {
            for (boolean revealed : new boolean[] {false, true}) {
                for (boolean energized : new boolean[] {false, true}) {
                    NodeRenderState state = state(type, 20);
                    state.depthIgnore = revealed;
                    state.energized = energized;
                    List<Layer> layers = layers(state);
                    assertEquals(energized ? 22 : 21, layers.size());
                    Layer core = layers.get(20);
                    int expectedStrip =
                            switch (type) {
                                case NORMAL -> 7;
                                case UNSTABLE -> 6;
                                case DARK -> 2;
                                case TAINTED -> 5;
                                case PURE -> 9;
                                case HUNGRY -> 8;
                            };
                    assertEquals(expectedStrip, core.strip());
                    boolean translucent = type == NodeType.DARK || type == NodeType.TAINTED;
                    RenderType expectedType = translucent
                            ? (revealed
                                    ? TTRenderTypes.fxTranslucentNoDepth(TEXTURE)
                                    : TTRenderTypes.fxTranslucent(TEXTURE))
                            : (revealed ? TTRenderTypes.fxAdditiveNoDepth(TEXTURE) : TTRenderTypes.fxAdditive(TEXTURE));
                    assertSame(expectedType, core.type());
                    for (Layer layer : layers) {
                        assertTrue(Float.isFinite(layer.scale()) && layer.scale() > 0.0F);
                        assertTrue(Float.isFinite(layer.alpha()));
                    }
                    if (energized) {
                        assertEquals(6, layers.get(21).strip());
                    }
                }
            }
        }
    }

    @Test
    void unaidedRenderingKeepsTheFaintStarWithoutLeakingAspectLayers() {
        try (var iris = mockStatic(IrisCompat.class)) {
            for (boolean shaders : new boolean[] {false, true}) {
                iris.when(IrisCompat::overridesShaders).thenReturn(shaders);
                NodeRenderState state = state(NodeType.HUNGRY, 6);
                state.visible = false;
                List<Layer> layers = layers(state);
                assertEquals(1, layers.size());
                assertEquals(7, layers.getFirst().strip());
                assertSame(TTRenderTypes.fxAdditive(TEXTURE), layers.getFirst().type());
                assertEquals(shaders ? 0.4F : 0.0066F, layers.getFirst().alpha());
            }
        }
    }

    @Test
    void revealedNodeWithoutAspectsStillRendersThroughBlocks() {
        NodeRenderState state = state(NodeType.NORMAL, 0);
        state.depthIgnore = true;

        List<Layer> layers = layers(state);

        assertEquals(1, layers.size());
        assertSame(TTRenderTypes.fxAdditiveNoDepth(TEXTURE), layers.getFirst().type());
    }

    @Test
    void jarSizeScalesEveryShellAndCoreTogether() {
        NodeRenderState state = state(NodeType.PURE, 3);
        List<Layer> fullSize = layers(state);
        state.jarred = true;
        state.size = 0.7F;
        List<Layer> jarSize = layers(state);
        for (int i = 0; i < fullSize.size(); i++) {
            assertEquals(fullSize.get(i).scale() * 0.7F, jarSize.get(i).scale(), 0.00001F);
            assertSame(fullSize.get(i).type(), jarSize.get(i).type());
        }
    }

    private static NodeRenderState state(NodeType type, int count) {
        NodeRenderState state = new NodeRenderState();
        state.type = type;
        state.visible = true;
        state.alpha = 0.7F;
        state.ticks = 124.5F;
        state.frameSeed = -987;
        for (int i = 0; i < count; i++) {
            NodeRenderState.AspectLayer aspect = new NodeRenderState.AspectLayer();
            aspect.amount = 25;
            aspect.color = 0xFF8844;
            aspect.blend = i % 2 == 0 ? 1 : 771;
            state.layers.add(aspect);
        }
        return state;
    }

    private static List<Layer> layers(NodeRenderState state) {
        List<Layer> layers = new ArrayList<>();
        NodeRenderer.forEachLayer(state, (index, type, angle, scale, alpha, color, strip, frame) -> {
            assertTrue(frame >= 0 && frame < 32);
            assertTrue(Float.isFinite(angle));
            layers.add(new Layer(type, scale, alpha, strip));
        });
        return layers;
    }
}
