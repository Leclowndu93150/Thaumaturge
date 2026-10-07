package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.FireBatModel;
import com.leclowndu93150.thaumaturge.client.render.entity.TintBufferSource;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpellBat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;

public final class SpellBatRenderer extends MobRenderer<EntitySpellBat, FireBatModel<EntitySpellBat>> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/spellbat.png");
    private static final float SHADOW = 0.25F;
    private static final float SCALE = 1.0F;
    private static final float Y_OFFSET = -0.1F;
    private static final int TINT_ALPHA = 128;
    private static final int FULLBRIGHT_BLOCK_LIGHT = 15;

    public SpellBatRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new FireBatModel<>(context.bakeLayer(TTModelLayers.FIRE_BAT), RenderType::entityTranslucent),
                SHADOW);
    }

    @Override
    public void render(
            EntitySpellBat entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        MultiBufferSource tinted = new TintBufferSource(buffers, ARGB32.color(TINT_ALPHA, entity.getColor()));
        super.render(entity, entityYaw, partialTick, poseStack, tinted, light);
    }

    @Override
    protected int getBlockLightLevel(EntitySpellBat entity, BlockPos pos) {
        return FULLBRIGHT_BLOCK_LIGHT;
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySpellBat entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EntitySpellBat entity, PoseStack poseStack, float partialTick) {
        poseStack.translate(0.0F, Y_OFFSET, 0.0F);
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
