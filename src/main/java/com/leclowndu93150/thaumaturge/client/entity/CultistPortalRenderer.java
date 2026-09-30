package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.OccludingEffectRenderer;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CultistPortalRenderer extends EntityRenderer<EntityCultistPortalLesser> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/misc/cultist_portal.png");

    private static final int FRAMES = 16;
    private static final float FRAME_WIDTH = 0.0625F;
    private static final float BASE_SCALE_Y = 1.4F;
    private static final float SCALE_FACTOR = 1.25F;
    private static final float GROW_TICKS = 50.0F;
    private static final int LIGHT = 0x00F000DC;

    public CultistPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            EntityCultistPortalLesser entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        queuePortal(
                entity.getPosition(partialTicks),
                entity.isActive(),
                entity.activeCounter + partialTicks,
                entity.hurtTime,
                entity.pulse,
                entity.getHealth() / entity.getMaxHealth(),
                entity.getBbHeight() / 2.0F,
                BASE_SCALE_Y,
                SCALE_FACTOR);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultistPortalLesser entity) {
        return TEXTURE;
    }

    static void queuePortal(
            Vec3 position,
            boolean active,
            float activeCounter,
            int hurtTime,
            int pulse,
            float healthFraction,
            float halfHeight,
            float baseScaleY,
            float scaleFactor) {
        if (!active) {
            return;
        }
        float scaleY = baseScaleY;
        float grown = Math.min(GROW_TICKS, activeCounter);
        if (hurtTime > 0) {
            double d = Math.sin(hurtTime * 72.0 * Math.PI / 180.0);
            scaleY -= (float) (d / 4.0);
            grown += (float) (6.0 * d);
        }
        if (pulse > 0) {
            double d = Math.sin(pulse * 36.0 * Math.PI / 180.0);
            scaleY += (float) (d / 4.0);
            grown += (float) (12.0 * d);
        }
        float scale = grown / GROW_TICKS * scaleFactor;
        float m = (1.0F - healthFraction) / 3.0F;
        float bob = Mth.sin(activeCounter / (5.0F - 12.0F * m)) * m + m;
        float bob2 = Mth.sin(activeCounter / (6.0F - 15.0F * m)) * m + m;
        float alpha = 1.0F - bob;
        scaleY -= bob / 4.0F;
        scale -= bob2 / 3.0F;
        int frame = FRAMES - 1 - (int) activeCounter % FRAMES;
        float u0 = frame * FRAME_WIDTH;
        float u1 = u0 + FRAME_WIDTH;
        int tint = ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F);
        OccludingEffectRenderer.enqueuePortal(position.add(0.0, halfHeight, 0.0), scale, scaleY, u0, u1, tint, LIGHT);
    }
}
