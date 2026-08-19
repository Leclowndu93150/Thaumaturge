package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.CrossbowModel;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public final class TurretCrossbowRenderer extends MobRenderer<EntityTurretCrossbow, TurretCrossbowRenderState, CrossbowModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/crossbow.png");
    private static final float SHADOW = 0.5F;

    public TurretCrossbowRenderer(EntityRendererProvider.Context context) {
        super(context, new CrossbowModel(context.bakeLayer(TCModelLayers.TURRET_CROSSBOW)), SHADOW);
    }

    @Override
    public TurretCrossbowRenderState createRenderState() {
        return new TurretCrossbowRenderState();
    }

    @Override
    public void extractRenderState(EntityTurretCrossbow entity, TurretCrossbowRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = Mth.wrapDegrees(Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot));
        state.bodyRot = 0.0F;
        state.swingAnim = entity.swingAnim;
        state.loadProgress = entity.getLoadProgress(partialTicks);
        state.ridingMinecart = entity.getVehicle() instanceof AbstractMinecart;
        state.hurtTime = entity.hurtTime;
    }

    @Override
    public Identifier getTextureLocation(TurretCrossbowRenderState state) {
        return TEXTURE;
    }
}
