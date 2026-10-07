package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGuardianModel;
import com.leclowndu93150.thaumaturge.client.render.entity.TintBufferSource;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class EldritchGuardianRenderer
        extends MobRenderer<EntityEldritchGuardian, EldritchGuardianModel<EntityEldritchGuardian>> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/eldritch_guardian.png");
    private static final float SHADOW = 0.5F;
    private static final float NEAR_ALPHA = 0.6F;
    private static final double NEAR_RANGE_SQ = 256.0;
    private static final double FAR_RANGE_HARD_SQ = 576.0;
    private static final double FAR_RANGE_SQ = 1024.0;

    public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel<>(context.bakeLayer(TTModelLayers.ELDRITCH_GUARDIAN)), SHADOW);
    }

    @Override
    public void render(
            EntityEldritchGuardian entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        float alpha = computeAlpha(entity);
        MultiBufferSource tinted = new TintBufferSource(buffers, ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));
        super.render(entity, entityYaw, partialTick, poseStack, tinted, light);
    }

    private static float computeAlpha(EntityEldritchGuardian entity) {
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        if (viewer == null) {
            return NEAR_ALPHA;
        }
        double far = entity.level().getDifficulty() == Difficulty.HARD ? FAR_RANGE_HARD_SQ : FAR_RANGE_SQ;
        double distSq = entity.distanceToSqr(viewer.getX(), viewer.getY(), viewer.getZ());
        if (distSq < NEAR_RANGE_SQ) {
            return NEAR_ALPHA;
        }
        return (float) (1.0 - Math.min(far - NEAR_RANGE_SQ, distSq - NEAR_RANGE_SQ) / (far - NEAR_RANGE_SQ))
                * NEAR_ALPHA;
    }

    @Override
    protected @Nullable RenderType getRenderType(
            EntityEldritchGuardian entity, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        return RenderType.itemEntityTranslucentCull(getTextureLocation(entity));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEldritchGuardian entity) {
        return TEXTURE;
    }
}
