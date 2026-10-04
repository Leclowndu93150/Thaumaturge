package com.leclowndu93150.thaumaturge.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

public abstract class ScaledMobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends MobRenderer<T, S, M> {
    private final Identifier skin;
    private final float size;

    protected ScaledMobRenderer(EntityRendererProvider.Context context, M model, Identifier skin, float size, float shadow) {
        super(context, model, shadow);
        this.skin = skin;
        this.size = size;
    }

    @Override
    protected void scale(S state, PoseStack poseStack) {
        poseStack.scale(size, size, size);
    }

    @Override
    public Identifier getTextureLocation(S state) {
        return skin;
    }
}
