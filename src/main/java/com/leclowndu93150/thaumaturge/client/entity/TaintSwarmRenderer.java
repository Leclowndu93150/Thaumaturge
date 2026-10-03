package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSwarm;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class TaintSwarmRenderer extends EntityRenderer<EntityTaintSwarm> {

    public TaintSwarmRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTaintSwarm entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
