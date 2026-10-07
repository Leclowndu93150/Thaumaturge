package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeSwarmerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;

public final class TaintSporeSwarmerCoreLayer extends RenderLayer<TaintSporeRenderState, TaintSporeSwarmerModel> {
    private static final RenderType CORE_GLOW = RenderTypes.entityTranslucentEmissive(AbstractTaintSporeRenderer.TEXTURE);

    private final TaintSporeSwarmerModel core;

    public TaintSporeSwarmerCoreLayer(RenderLayerParent<TaintSporeRenderState, TaintSporeSwarmerModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.core = new TaintSporeSwarmerModel(modelSet.bakeLayer(TTModelLayers.TAINT_SPORE_SWARMER_CORE));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, TaintSporeRenderState state, float yRot, float xRot) {
        collector.order(1).submitModel(core, state, poseStack, CORE_GLOW, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
