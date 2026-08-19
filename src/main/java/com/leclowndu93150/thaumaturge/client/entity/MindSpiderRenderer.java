package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

public final class MindSpiderRenderer extends MobRenderer<EntityMindSpider, MindSpiderRenderState, SpiderModel> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/spider/spider.png");
    private static final float SHADOW = 0.5F;
    private static final float SCALE = 0.6F;
    private static final float MAX_ALPHA = 0.1F;
    private static final float FADE_IN_TICKS = 100.0F;

    public MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), SHADOW);
        this.addLayer(new GhostSpiderEyesLayer(this));
    }

    private static final class GhostSpiderEyesLayer extends RenderLayer<MindSpiderRenderState, SpiderModel> {
        private static final RenderType SPIDER_EYES = RenderTypes.eyes(Identifier.withDefaultNamespace("textures/entity/spider/spider_eyes.png"));

        GhostSpiderEyesLayer(RenderLayerParent<MindSpiderRenderState, SpiderModel> renderer) {
            super(renderer);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, MindSpiderRenderState state, float yRot, float xRot) {
            collector.order(1).submitModel(this.getParentModel(), state, poseStack, SPIDER_EYES, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }

    @Override
    public MindSpiderRenderState createRenderState() {
        return new MindSpiderRenderState();
    }

    @Override
    public void extractRenderState(EntityMindSpider entity, MindSpiderRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        String viewer = entity.getViewer();
        String localName = Minecraft.getInstance().player == null ? "" : Minecraft.getInstance().player.getGameProfile().name();
        state.hiddenFromViewer = !viewer.isEmpty() && !viewer.equals(localName);
    }

    @Override
    public void submit(MindSpiderRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.hiddenFromViewer) {
            return;
        }
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected void scale(MindSpiderRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected @Nullable RenderType getRenderType(MindSpiderRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        return super.getRenderType(state, isBodyVisible, true, appearGlowing);
    }

    @Override
    protected int getModelTint(MindSpiderRenderState state) {
        float alpha = Math.min(MAX_ALPHA, state.ageInTicks / FADE_IN_TICKS);
        return ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public Identifier getTextureLocation(MindSpiderRenderState state) {
        return TEXTURE;
    }
}
