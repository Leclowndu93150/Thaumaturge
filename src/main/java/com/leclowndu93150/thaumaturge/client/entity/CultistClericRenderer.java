package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.OccludingEffectRenderer;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistCleric;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CultistClericRenderer
        extends HumanoidMobRenderer<EntityCultistCleric, HumanoidModel<EntityCultistCleric>> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/cultist.png");
    private static final float SHADOW = 0.5F;
    private static final int BOB_PHASE_RANGE = 1000;
    private static final float BOB_PERIOD = 9.0F;
    private static final float BOB_AMPLITUDE = 0.1F;
    private static final float BOB_BASE = 0.21F;
    private static final float LINE_START_EYE_SCALE = 1.2F;
    private static final double LINE_END_HEIGHT = 1.5;
    private static final int LINE_COLOR = 0x110011;
    private static final float LINE_SPEED = -0.03F;
    private static final float LINE_WIDTH = 0.25F;
    private static final float LINE_FADE_IN_TICKS = 10.0F;

    public CultistClericRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(TCModelLayers.CULTIST)), SHADOW);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(
            EntityCultistCleric entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        if (!entity.isRitualist()) {
            super.render(entity, entityYaw, partialTicks, poseStack, buffers, light);
            return;
        }
        int phase = Math.floorMod(entity.getId(), BOB_PHASE_RANGE);
        float cycle = entity.tickCount + partialTicks + phase;
        float bob = Mth.sin(cycle / BOB_PERIOD) * BOB_AMPLITUDE + BOB_BASE;
        float lineStartY = entity.getEyeHeight() * LINE_START_EYE_SCALE;
        BlockPos anchor = entity.ritualAnchor();
        Vec3 position = entity.getPosition(partialTicks);
        Vec3 altarFromEntity = new Vec3(
                anchor.getX() + 0.5 - position.x,
                anchor.getY() + LINE_END_HEIGHT - bob - position.y,
                anchor.getZ() + 0.5 - position.z);
        Vec3 clericFromAltar = new Vec3(-altarFromEntity.x, lineStartY - altarFromEntity.y, -altarFromEntity.z);
        float time = FloatyLineRenderer.time(entity.level().getGameTime(), partialTicks);
        float lineFade = Math.min(entity.tickCount, LINE_FADE_IN_TICKS) / LINE_FADE_IN_TICKS;
        poseStack.pushPose();
        poseStack.translate(0.0F, bob, 0.0F);
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, light);
        poseStack.popPose();
        OccludingEffectRenderer.enqueueBeam(
                position,
                new Vec3(anchor.getX() + 0.5, anchor.getY() + LINE_END_HEIGHT, anchor.getZ() + 0.5),
                clericFromAltar,
                time,
                LINE_COLOR,
                LINE_SPEED,
                lineFade,
                LINE_WIDTH);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultistCleric entity) {
        return TEXTURE;
    }
}
