package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGuardianModel;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchWarden;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class EldritchWardenRenderer
        extends MobRenderer<EntityEldritchWarden, EldritchGuardianModel<EntityEldritchWarden>> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/eldritch_warden.png");
    private static final float SHADOW = 0.5F;
    private static final float SPAWN_TICKS = 150.0F;

    public EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel<>(context.bakeLayer(TTModelLayers.ELDRITCH_GUARDIAN)), SHADOW);
    }

    @Override
    public void render(
            EntityEldritchWarden entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        float spawnFraction = entity.getSpawnTimer() / SPAWN_TICKS;
        poseStack.pushPose();
        if (spawnFraction > 0.0F) {
            poseStack.translate(0.0F, -entity.getBbHeight() * spawnFraction, 0.0F);
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEldritchWarden entity) {
        return TEXTURE;
    }
}
