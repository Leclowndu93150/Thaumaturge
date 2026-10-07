package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTFXPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class EssentiaStreamRenderType {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TTIds.MODID, "textures/effect/essentia.png");

    public static final RenderPipeline PIPELINE = TTFXPipelines.translucentTextured(Identifier.fromNamespaceAndPath(TTIds.MODID, "pipeline/essentia_stream"));

    public static final RenderType RENDER_TYPE = RenderType.create("thaumaturge_essentia_stream", RenderSetup.builder(PIPELINE).withTexture("Sampler0", TEXTURE).createRenderSetup());

    @SubscribeEvent
    static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    private EssentiaStreamRenderType() {}
}
