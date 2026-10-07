package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeModel;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSpore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public final class TaintSporeRenderer extends AbstractTaintSporeRenderer<EntityTaintSpore, TaintSporeModel> {
    private static final float SIZE_SCALE = 0.12F;
    private static final float PULSE_SCALE = 0.025F;
    private static final float PULSE_RATE = 0.075F;
    private static final float MIN_SCALE = 0.01F;

    public TaintSporeRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintSporeModel(context.bakeLayer(TTModelLayers.TAINT_SPORE)));
    }

    @Override
    protected void scale(TaintSporeRenderState state, PoseStack poseStack) {
        float size = SIZE_SCALE * state.displaySize;
        float pulse = PULSE_SCALE * Mth.sin(state.ageInTicks * PULSE_RATE);
        float horizontal = Math.max(MIN_SCALE, size + pulse);
        poseStack.scale(horizontal, Math.max(MIN_SCALE, size - pulse), horizontal);
    }
}
