package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirrorBase;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityCentrifuge;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockEntityBellows;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.BlockEntityEssentiaCrystalizer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeValve;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class BakedBlockEntityRenderTest {
    private enum Renderer {
        CENTRIFUGE,
        BELLOWS,
        CRYSTALIZER,
        MIRROR,
        VALVE
    }

    @ParameterizedTest
    @CsvSource({
        "CENTRIFUGE,false", "CENTRIFUGE,true",
        "BELLOWS,false", "BELLOWS,true",
        "CRYSTALIZER,false", "CRYSTALIZER,true",
        "MIRROR,false", "MIRROR,true",
        "VALVE,false", "VALVE,true"
    })
    void bakedPartsUseEntityBuffersButKeepChunkLayersForQuadSelection(Renderer renderer, boolean shaderTransparency) {
        try (Fixture fixture = new Fixture(shaderTransparency)) {
            fixture.render(renderer);
            ArgumentCaptor<RenderType> buffers = ArgumentCaptor.forClass(RenderType.class);
            verify(fixture.buffers, atLeastOnce()).getBuffer(buffers.capture());
            for (RenderType type : buffers.getAllValues()) {
                assertSame(DefaultVertexFormat.NEW_ENTITY, type.format(), type.toString());
            }
            verify(fixture.buffers, atLeastOnce()).getBuffer(Sheets.cutoutBlockSheet());
            verify(fixture.buffers, atLeastOnce())
                    .getBuffer(shaderTransparency ? Sheets.translucentItemSheet() : Sheets.translucentCullBlockSheet());
            for (RenderType chunkLayer : fixture.layers) {
                if (renderer == Renderer.CRYSTALIZER) {
                    verify(fixture.model, atLeastOnce())
                            .getQuads(eq(fixture.state), isNull(), any(), eq(ModelData.EMPTY), eq(chunkLayer));
                } else {
                    verify(fixture.modelRenderer, atLeastOnce())
                            .renderModel(
                                    any(),
                                    any(),
                                    eq(fixture.state),
                                    eq(fixture.model),
                                    anyFloat(),
                                    anyFloat(),
                                    anyFloat(),
                                    anyInt(),
                                    anyInt(),
                                    eq(ModelData.EMPTY),
                                    eq(chunkLayer));
                }
            }
        }
    }

    @Test
    void crystalizerOnlyColorsQuadsUsingItsAspectTintIndex() {
        try (Fixture fixture = new Fixture(false)) {
            BakedQuad tinted = mock(BakedQuad.class);
            when(tinted.isTinted()).thenReturn(true);
            when(tinted.getTintIndex()).thenReturn(0);
            BakedQuad untinted = mock(BakedQuad.class);
            when(untinted.getTintIndex()).thenReturn(-1);
            BakedQuad otherTint = mock(BakedQuad.class);
            when(otherTint.isTinted()).thenReturn(true);
            when(otherTint.getTintIndex()).thenReturn(1);
            when(fixture.model.getQuads(eq(fixture.state), isNull(), any(), eq(ModelData.EMPTY), any()))
                    .thenReturn(List.of(tinted, untinted, otherTint));
            fixture.render(Renderer.CRYSTALIZER);
            verify(fixture.consumer, atLeastOnce())
                    .putBulkData(any(), eq(tinted), eq(0.2F), eq(0.4F), eq(0.6F), eq(1.0F), anyInt(), anyInt());
            for (BakedQuad quad : List.of(untinted, otherTint)) {
                verify(fixture.consumer, atLeastOnce())
                        .putBulkData(any(), eq(quad), eq(1.0F), eq(1.0F), eq(1.0F), eq(1.0F), anyInt(), anyInt());
            }
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final BlockState state = Blocks.DISPENSER.defaultBlockState();
        private final List<RenderType> layers =
                List.of(RenderType.solid(), RenderType.cutout(), RenderType.translucent());
        private final BakedModel model = mock(BakedModel.class);
        private final ModelBlockRenderer modelRenderer = mock(ModelBlockRenderer.class);
        private final MultiBufferSource buffers = mock(MultiBufferSource.class);
        private final VertexConsumer consumer = mock(VertexConsumer.class, RETURNS_SELF);
        private final MockedStatic<Minecraft> minecraftStatic;

        private Fixture(boolean shaderTransparency) {
            Minecraft minecraft = mock(Minecraft.class);
            ModelManager models = mock(ModelManager.class);
            BlockRenderDispatcher blocks = mock(BlockRenderDispatcher.class);
            when(minecraft.getModelManager()).thenReturn(models);
            when(minecraft.getBlockRenderer()).thenReturn(blocks);
            when(blocks.getModelRenderer()).thenReturn(modelRenderer);
            when(models.getModel(any())).thenReturn(model);
            when(model.getRenderTypes(eq(state), any(), eq(ModelData.EMPTY))).thenReturn(ChunkRenderTypeSet.of(layers));
            when(buffers.getBuffer(any())).thenReturn(consumer);
            minecraftStatic = mockStatic(Minecraft.class);
            minecraftStatic.when(Minecraft::getInstance).thenReturn(minecraft);
            minecraftStatic.when(Minecraft::useShaderTransparency).thenReturn(shaderTransparency);
        }

        private void render(Renderer renderer) {
            PoseStack pose = new PoseStack();
            switch (renderer) {
                case CENTRIFUGE -> {
                    BlockEntityCentrifuge entity = mock(BlockEntityCentrifuge.class);
                    when(entity.getBlockState()).thenReturn(state);
                    new CentrifugeRenderer(null).render(entity, 0.5F, pose, buffers, 0, OverlayTexture.NO_OVERLAY);
                }
                case BELLOWS -> {
                    BlockEntityBellows entity = mock(BlockEntityBellows.class);
                    when(entity.getBlockState()).thenReturn(state);
                    entity.inflation = 0.5F;
                    new BellowsRenderer(null).render(entity, 0.5F, pose, buffers, 0, OverlayTexture.NO_OVERLAY);
                }
                case CRYSTALIZER -> {
                    BlockEntityEssentiaCrystalizer entity = mock(BlockEntityEssentiaCrystalizer.class);
                    when(entity.getBlockState()).thenReturn(state);
                    when(entity.getLevel()).thenReturn(mock(Level.class));
                    when(entity.aspectKey()).thenReturn(TTAspects.AER);
                    entity.crystalRed = 0.2F;
                    entity.crystalGreen = 0.4F;
                    entity.crystalBlue = 0.6F;
                    new EssentiaCrystalizerRenderer(null)
                            .render(entity, 0.5F, pose, buffers, 0, OverlayTexture.NO_OVERLAY);
                }
                case MIRROR -> {
                    BlockEntityMirrorBase entity = mock(BlockEntityMirrorBase.class);
                    when(entity.getBlockState()).thenReturn(state);
                    new MirrorRenderer(null).render(entity, 0.5F, pose, buffers, 0, OverlayTexture.NO_OVERLAY);
                }
                case VALVE -> {
                    BlockEntityTubeValve entity = mock(BlockEntityTubeValve.class);
                    when(entity.getBlockState()).thenReturn(state);
                    when(entity.facing()).thenReturn(Direction.UP);
                    new TubeValveRenderer(null).render(entity, 0.5F, pose, buffers, 0, OverlayTexture.NO_OVERLAY);
                }
            }
        }

        @Override
        public void close() {
            minecraftStatic.close();
        }
    }
}
