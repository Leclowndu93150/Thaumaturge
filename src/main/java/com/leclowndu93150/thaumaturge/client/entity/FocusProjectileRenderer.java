package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityFocusProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class FocusProjectileRenderer extends EntityRenderer<EntityFocusProjectile> {
    private static final float SHADOW = 0.1F;

    public FocusProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW;
    }

    @Override
    public void render(
            EntityFocusProjectile entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        entity.renderParticle(partialTicks);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFocusProjectile entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
