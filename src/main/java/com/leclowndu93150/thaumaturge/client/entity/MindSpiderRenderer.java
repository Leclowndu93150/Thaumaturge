package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.render.entity.TintBufferSource;
import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import org.jspecify.annotations.Nullable;

public final class MindSpiderRenderer extends MobRenderer<EntityMindSpider, SpiderModel<EntityMindSpider>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/spider/spider.png");
    private static final float SHADOW = 0.5F;
    private static final float SCALE = 0.6F;
    private static final float MAX_ALPHA = 0.1F;
    private static final float FADE_IN_TICKS = 100.0F;

    public MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), SHADOW);
        this.addLayer(new GhostSpiderEyesLayer(this));
    }

    @Override
    public void render(
            EntityMindSpider entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        if (isHiddenFromViewer(entity)) {
            return;
        }
        float alpha = Math.min(MAX_ALPHA, (entity.tickCount + partialTick) / FADE_IN_TICKS);
        MultiBufferSource tinted = new TintBufferSource(buffers, ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));
        super.render(entity, entityYaw, partialTick, poseStack, tinted, light);
    }

    private static boolean isHiddenFromViewer(EntityMindSpider entity) {
        String viewer = entity.getViewer();
        String localName = Minecraft.getInstance().player == null
                ? ""
                : Minecraft.getInstance().player.getGameProfile().getName();
        return !viewer.isEmpty() && !viewer.equals(localName);
    }

    @Override
    protected void scale(EntityMindSpider entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected @Nullable RenderType getRenderType(
            EntityMindSpider entity, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMindSpider entity) {
        return TEXTURE;
    }

    private static final class GhostSpiderEyesLayer
            extends RenderLayer<EntityMindSpider, SpiderModel<EntityMindSpider>> {
        private static final RenderType SPIDER_EYES =
                RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/entity/spider_eyes.png"));

        GhostSpiderEyesLayer(RenderLayerParent<EntityMindSpider, SpiderModel<EntityMindSpider>> renderer) {
            super(renderer);
        }

        @Override
        public void render(
                PoseStack poseStack,
                MultiBufferSource buffers,
                int packedLight,
                EntityMindSpider entity,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch) {
            this.getParentModel()
                    .renderToBuffer(
                            poseStack, buffers.getBuffer(SPIDER_EYES), packedLight, OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
