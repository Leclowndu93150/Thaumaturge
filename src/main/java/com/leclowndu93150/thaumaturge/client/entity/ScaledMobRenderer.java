package com.leclowndu93150.thaumaturge.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public abstract class ScaledMobRenderer<T extends Mob, M extends EntityModel<T>> extends MobRenderer<T, M> {
    private final ResourceLocation skin;
    private final float size;

    protected ScaledMobRenderer(
            EntityRendererProvider.Context context, M model, ResourceLocation skin, float size, float shadow) {
        super(context, model, shadow);
        this.skin = skin;
        this.size = size;
    }

    @Override
    protected void scale(T entity, PoseStack pose, float partialTick) {
        pose.scale(size, size, size);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return skin;
    }
}
