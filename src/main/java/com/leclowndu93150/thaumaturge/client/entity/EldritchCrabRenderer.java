package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchCrabModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchCrab;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class EldritchCrabRenderer extends MobRenderer<EntityEldritchCrab, EldritchCrabRenderState, EldritchCrabModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/crab.png");
    private static final float SHADOW = 0.5F;
    private static final float SCALE = 0.8F;

    public EldritchCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchCrabModel(context.bakeLayer(TCModelLayers.ELDRITCH_CRAB)), SHADOW);
    }

    @Override
    public EldritchCrabRenderState createRenderState() {
        return new EldritchCrabRenderState();
    }

    @Override
    public void extractRenderState(EntityEldritchCrab entity, EldritchCrabRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.helm = entity.hasHelm();
    }

    @Override
    protected void scale(EldritchCrabRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public Identifier getTextureLocation(EldritchCrabRenderState state) {
        return TEXTURE;
    }
}
