package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;

public final class TaintSheepRenderer extends SheepRenderer {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/taint_sheep.png");

    public TaintSheepRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Sheep entity) {
        return TEXTURE;
    }
}
