package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGolemModel;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

public final class EldritchGolemRenderer extends ScaledMobRenderer<EntityEldritchGolem, EldritchGolemRenderState, EldritchGolemModel> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/eldritch_golem.png");
    private static final float GOLEM_SIZE = 1.0F;
    private static final float GOLEM_SHADOW = 0.7F;

    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGolemModel(context.bakeLayer(TTModelLayers.ELDRITCH_GOLEM)), TEXTURE, GOLEM_SIZE, GOLEM_SHADOW);
        this.addLayer(new CoreLayer(this, context.bakeLayer(TTModelLayers.ELDRITCH_GOLEM)));
    }

    @Override
    public EldritchGolemRenderState createRenderState() {
        return new EldritchGolemRenderState();
    }

    @Override
    public void extractRenderState(EntityEldritchGolem golem, EldritchGolemRenderState state, float partialTicks) {
        super.extractRenderState(golem, state, partialTicks);
        state.headless = golem.isHeadless();
        state.attackTime = Math.max(0.0F, golem.swingCooldown() - partialTicks);
        state.spawnTimer = golem.getSpawnTimer();
    }

    private static final class CoreLayer extends RenderLayer<EldritchGolemRenderState, EldritchGolemModel> {
        private static final RenderType CORE_GLOW = RenderTypes.entityTranslucentEmissive(TEXTURE);

        private final EldritchGolemModel core;

        CoreLayer(RenderLayerParent<EldritchGolemRenderState, EldritchGolemModel> renderer, ModelPart root) {
            super(renderer);
            this.core = new EldritchGolemModel(root, EldritchGolemModel.Material.CORE);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, EldritchGolemRenderState state, float yRot, float xRot) {
            if (state.isInvisible) {
                return;
            }
            collector.order(1).submitModel(core, state, poseStack, CORE_GLOW, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
