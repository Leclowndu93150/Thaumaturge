package com.leclowndu93150.thaumaturge.client.render;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class TTFlatRenderTypes {
    private TTFlatRenderTypes() {}

    public static RenderType entityCutoutFlat(ResourceLocation texture) {
        return TTRenderTypes.entityCutoutFlat(texture);
    }

    public static RenderType entityTranslucentFlat(ResourceLocation texture) {
        return TTRenderTypes.entityTranslucentFlat(texture);
    }

    public static RenderType entityAdditiveFlat(ResourceLocation texture) {
        return TTRenderTypes.entityAdditiveEmissive(texture);
    }
}
