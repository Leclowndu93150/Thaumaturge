package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.entity.TintBufferSource;
import com.leclowndu93150.thaumaturge.content.entity.EntityBrainyZombie;
import com.leclowndu93150.thaumaturge.content.entity.EntityGiantBrainyZombie;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;

public final class BrainyZombieRenderer
        extends AbstractZombieRenderer<EntityBrainyZombie, ZombieModel<EntityBrainyZombie>> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/brainy_zombie.png");
    private static final float ANGER_TINT_STRENGTH = 0.5F;

    public BrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
    }

    @Override
    public void render(
            EntityBrainyZombie entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        float anger = entity instanceof EntityGiantBrainyZombie giant ? giant.getAnger() : 0.0F;
        if (anger > 0.0F) {
            float fade = 1.0F - Math.min(1.0F, anger) * ANGER_TINT_STRENGTH;
            int tint = ARGB32.colorFromFloat(1.0F, 1.0F, Mth.clamp(fade, 0.0F, 1.0F), Mth.clamp(fade, 0.0F, 1.0F));
            buffers = new TintBufferSource(buffers, tint);
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBrainyZombie entity) {
        return TEXTURE;
    }
}
