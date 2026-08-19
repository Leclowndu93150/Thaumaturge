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
public final class BeamRenderType {
    public static final Identifier BEAM = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/effect/beam1.png");
    public static final Identifier BEAML = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/effect/beaml.png");
    public static final Identifier BEAMH = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/effect/beamh.png");
    public static final Identifier NODE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/effect/auranodes.png");

    public static final RenderPipeline PIPELINE = TCFXPipelines.additiveTextured(Identifier.fromNamespaceAndPath(TCIds.MODID, "pipeline/beam"));

    public static final RenderType TRUNK_BEAM = makeType("thaumaturge_beam_trunk_beam", BEAM);
    public static final RenderType TRUNK_BEAML = makeType("thaumaturge_beam_trunk_beaml", BEAML);
    public static final RenderType TRUNK_BEAMH = makeType("thaumaturge_beam_trunk_beamh", BEAMH);
    public static final RenderType NODE_TYPE = makeType("thaumaturge_beam_node", NODE);

    public static RenderType trunkForType(int type) {
        return switch (type) {
            case 1 -> TRUNK_BEAML;
            case 2 -> TRUNK_BEAMH;
            default -> TRUNK_BEAM;
        };
    }

    private static RenderType makeType(String name, Identifier texture) {
        return RenderType.create(name, RenderSetup.builder(PIPELINE).withTexture("Sampler0", texture).createRenderSetup());
    }

    @SubscribeEvent
    static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    private BeamRenderType() {}
}
