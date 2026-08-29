package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

public final class TaintPigRenderer extends PigRenderer {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/taint_pig.png");

    public TaintPigRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Pig entity) {
        return TEXTURE;
    }
}
