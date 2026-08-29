package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSpore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** TC4-style translucent, slowly swelling Taint Spore renderer. */
public final class TaintSporeRenderer extends MobRenderer<EntityTaintSpore, TaintSporeModel<EntityTaintSpore>> {
    static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/taint_spore.png");
    private static final float SHADOW = 0.25F;
    private static final float SIZE_SCALE = 0.12F;
    private static final float PULSE_SCALE = 0.025F;

    public TaintSporeRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintSporeModel<>(context.bakeLayer(TCModelLayers.TAINT_SPORE)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTaintSpore entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EntityTaintSpore entity, PoseStack poseStack, float partialTick) {
        float age = entity.tickCount + partialTick;
        float size = SIZE_SCALE * entity.getDisplaySize(partialTick);
        float pulse = PULSE_SCALE * Mth.sin(age * 0.075F);
        float horizontal = Math.max(0.01F, size + pulse);
        float vertical = Math.max(0.01F, size - pulse);
        poseStack.scale(horizontal, vertical, horizontal);
    }
}
