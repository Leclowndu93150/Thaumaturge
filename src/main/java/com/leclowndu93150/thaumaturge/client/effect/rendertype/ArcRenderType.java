package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCFXPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ArcRenderType {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/effect/beamh.png");

    public static final RenderPipeline PIPELINE = TCFXPipelines.additiveTextured(Identifier.fromNamespaceAndPath(TCIds.MODID, "pipeline/arc"));

    public static final RenderType RENDER_TYPE = RenderType.create("thaumaturge_arc", RenderSetup.builder(PIPELINE).withTexture("Sampler0", TEXTURE).createRenderSetup());

    @SubscribeEvent
    static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    private ArcRenderType() {}
}
