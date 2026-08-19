package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGuardianModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class EldritchGuardianRenderer extends MobRenderer<EntityEldritchGuardian, EldritchGuardianRenderState, EldritchGuardianModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/eldritch_guardian.png");
    private static final float SHADOW = 0.5F;
    private static final float NEAR_ALPHA = 0.6F;
    private static final double NEAR_RANGE_SQ = 256.0;
    private static final double FAR_RANGE_HARD_SQ = 576.0;
    private static final double FAR_RANGE_SQ = 1024.0;

    public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel(context.bakeLayer(TCModelLayers.ELDRITCH_GUARDIAN)), SHADOW);
    }

    @Override
    public EldritchGuardianRenderState createRenderState() {
        return new EldritchGuardianRenderState();
    }

    @Override
    public void extractRenderState(EntityEldritchGuardian entity, EldritchGuardianRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.armLiftL = entity.armLiftL;
        state.armLiftR = entity.armLiftR;
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        if (viewer == null) {
            state.alpha = NEAR_ALPHA;
            return;
        }
        double far = entity.level().getDifficulty() == Difficulty.HARD ? FAR_RANGE_HARD_SQ : FAR_RANGE_SQ;
        double distSq = entity.distanceToSqr(viewer.getX(), viewer.getY(), viewer.getZ());
        if (distSq < NEAR_RANGE_SQ) {
            state.alpha = NEAR_ALPHA;
        } else {
            state.alpha = (float) (1.0 - Math.min(far - NEAR_RANGE_SQ, distSq - NEAR_RANGE_SQ) / (far - NEAR_RANGE_SQ)) * NEAR_ALPHA;
        }
    }

    @Override
    protected @Nullable RenderType getRenderType(EldritchGuardianRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        return super.getRenderType(state, isBodyVisible, true, appearGlowing);
    }

    @Override
    protected int getModelTint(EldritchGuardianRenderState state) {
        return ARGB.colorFromFloat(state.alpha, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public Identifier getTextureLocation(EldritchGuardianRenderState state) {
        return TEXTURE;
    }
}
