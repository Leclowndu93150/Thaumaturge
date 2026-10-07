package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class BeamRenderType {
    public static final ResourceLocation BEAM =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/effect/beam1.png");
    public static final ResourceLocation BEAML =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/effect/beaml.png");
    public static final ResourceLocation BEAMH =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/effect/beamh.png");
    public static final ResourceLocation NODE =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/effect/auranodes.png");

    public static final RenderType TRUNK_BEAM = TTRenderTypes.additiveTextured(BEAM);
    public static final RenderType TRUNK_BEAML = TTRenderTypes.additiveTextured(BEAML);
    public static final RenderType TRUNK_BEAMH = TTRenderTypes.additiveTextured(BEAMH);
    public static final RenderType NODE_TYPE = TTRenderTypes.additiveTextured(NODE);

    public static RenderType trunkForType(int type) {
        return switch (type) {
            case 1 -> TRUNK_BEAML;
            case 2 -> TRUNK_BEAMH;
            default -> TRUNK_BEAM;
        };
    }

    private BeamRenderType() {}
}
