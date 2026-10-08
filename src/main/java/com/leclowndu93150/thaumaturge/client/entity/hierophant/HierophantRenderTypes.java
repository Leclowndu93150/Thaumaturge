package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import net.minecraft.client.renderer.RenderType;

public final class HierophantRenderTypes {
    public static final RenderType EYE = RenderType.eyes(HierophantTextures.BOSS);
    public static final RenderType SPELL = TTRenderTypes.fxTranslucent(HierophantTextures.SPELLS);
    public static final RenderType ARC = TTRenderTypes.fxAdditive(HierophantTextures.ARC);
    public static final RenderType HAMMER = RenderType.entityCutout(HierophantTextures.BOSS);

    private HierophantRenderTypes() {}
}
