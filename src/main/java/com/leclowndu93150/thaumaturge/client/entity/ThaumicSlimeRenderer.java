package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class ThaumicSlimeRenderer extends MobRenderer<ThaumicSlime, SlimeRenderState, SlimeModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/entity/thaumic_slime.png");

    public ThaumicSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
        this.addLayer(new OuterLayer(this, context.getModelSet()));
    }

    @Override
    protected float getShadowRadius(SlimeRenderState state) {
        return state.size * 0.25F;
    }

    @Override
    protected void scale(SlimeRenderState state, PoseStack poseStack) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = state.size;
        float squish = state.squish / (size * 0.5F + 1.0F);
        float stretch = 1.0F / (squish + 1.0F);
        poseStack.scale(stretch * size, 1.0F / stretch * size, stretch * size);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(ThaumicSlime entity, SlimeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish);
        state.size = entity.getSize();
    }

    private static final class OuterLayer extends RenderLayer<SlimeRenderState, SlimeModel> {
        private final SlimeModel model;

        OuterLayer(RenderLayerParent<SlimeRenderState, SlimeModel> renderer, EntityModelSet modelSet) {
            super(renderer);
            this.model = new SlimeModel(modelSet.bakeLayer(ModelLayers.SLIME_OUTER));
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, SlimeRenderState state, float yRot, float xRot) {
            boolean glowingInvisible = state.appearsGlowing() && state.isInvisible;
            if (state.isInvisible && !glowingInvisible) {
                return;
            }
            int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
            if (glowingInvisible) {
                collector.order(1).submitModel(this.model, state, poseStack, RenderTypes.outline(TEXTURE), lightCoords, overlayCoords, state.outlineColor, null);
            } else {
                collector.order(1).submitModel(this.model, state, poseStack, RenderTypes.entityTranslucent(TEXTURE), lightCoords, overlayCoords, state.outlineColor, null);
            }
        }
    }
}
