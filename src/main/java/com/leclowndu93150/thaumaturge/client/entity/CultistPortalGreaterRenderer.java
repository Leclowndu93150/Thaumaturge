package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class CultistPortalGreaterRenderer extends EntityRenderer<EntityCultistPortalGreater> {
    private static final float BASE_SCALE_Y = 1.5F;
    private static final float SCALE_FACTOR = 1.3F;
    private static final float SHADOW = 0.1F;

    public CultistPortalGreaterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW;
    }

    @Override
    public void render(
            EntityCultistPortalGreater entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        CultistPortalRenderer.queuePortal(
                entity.getPosition(partialTicks),
                true,
                entity.tickCount + partialTicks,
                entity.hurtTime,
                entity.pulse,
                entity.getHealth() / entity.getMaxHealth(),
                entity.getBbHeight() / 2.0F,
                BASE_SCALE_Y,
                SCALE_FACTOR);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultistPortalGreater entity) {
        return TCIds.rl("textures/misc/cultist_portal.png");
    }
}
