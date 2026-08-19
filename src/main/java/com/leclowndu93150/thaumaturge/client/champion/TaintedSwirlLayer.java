package com.leclowndu93150.thaumaturge.client.champion;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionModifier;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class TaintedSwirlLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private static final Identifier TAINT_FIBRES = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/models/taint_fibres.png");
    private static final float ALPHA = 0.66F;
    private static final int COLOR = ARGB.colorFromFloat(ALPHA, 1.0F, 1.0F, 1.0F);
    private static final float U_FREQUENCY = 2.5E-4F;
    private static final float V_FACTOR = 0.001F;
    private static final float UV_SCALE_U = 8.0F;
    private static final float UV_SCALE_V = 4.0F;

    public TaintedSwirlLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
        if (((ChampionRenderState) state).thaumaturge$championType() != ChampionModifier.TAINTED) {
            return;
        }
        float id = ((ChampionRenderState) state).thaumaturge$entityId();
        float u = Mth.cos(id * U_FREQUENCY);
        float v = id * V_FACTOR;
        collector.order(1).submitModel(this.getParentModel(), state, poseStack, swirl(u, v, state.isInvisible), lightCoords, OverlayTexture.NO_OVERLAY, COLOR, null, state.outlineColor, null);
    }

    private static RenderType swirl(float u, float v, boolean invisible) {
        RenderPipeline pipeline = invisible ? TCRenderPipelines.TAINTED_SWIRL_NO_DEPTH : RenderPipelines.BREEZE_WIND;
        return RenderType.create("thaumaturge_tainted_swirl",
                RenderSetup.builder(pipeline).withTexture("Sampler0", TAINT_FIBRES)
                        .setTextureTransform(new TextureTransform("tainted_swirl", () -> new Matrix4f().scaling(UV_SCALE_U, UV_SCALE_V, 1.0F).translate(u, v, 0.0F))).useLightmap().sortOnUpload()
                        .createRenderSetup());
    }
}
