package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class HierophantRenderTypes {
    public static final RenderType EYE = RenderTypes.eyes(HierophantTextures.BOSS);
    public static final RenderType SPELL = RenderType.create("thaumaturge_eldritch_spell",
            RenderSetup.builder(TTRenderPipelines.FX_TRANSLUCENT).withTexture("Sampler0", HierophantTextures.SPELLS).useLightmap().createRenderSetup());
    public static final RenderType ARC = RenderType.create("thaumaturge_hierophant_arc",
            RenderSetup.builder(TTRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", HierophantTextures.ARC).useLightmap().createRenderSetup());
    public static final RenderType HAMMER = RenderTypes.entityCutout(HierophantTextures.BOSS);
    private HierophantRenderTypes() {}
}
